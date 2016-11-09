/*
 *
 *  Copyright 2012-2014 Eurocommercial Properties NV
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.estatio.integtests.invoice;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.wrapper.InvalidException;

import org.isisaddons.module.security.dom.tenancy.ApplicationTenancy;
import org.isisaddons.module.security.dom.tenancy.ApplicationTenancyRepository;

import org.incode.module.communications.dom.impl.commchannel.CommunicationChannel;

import org.estatio.app.menus.numerator.NumeratorForCollectionMenu;
import org.estatio.app.services.invoice.InvoiceImportLine;
import org.estatio.dom.asset.Property;
import org.estatio.dom.asset.PropertyRepository;
import org.estatio.dom.invoice.CommunicationChannelSubscriptions;
import org.estatio.dom.invoice.Invoice;
import org.estatio.dom.invoice.InvoiceItem;
import org.estatio.dom.invoice.InvoiceRepository;
import org.estatio.dom.invoice.InvoiceStatus;
import org.estatio.dom.invoice.PaymentMethod;
import org.estatio.dom.lease.Lease;
import org.estatio.dom.lease.LeaseRepository;
import org.estatio.dom.numerator.NumeratorRepository;
import org.estatio.dom.party.Party;
import org.estatio.dom.party.PartyRepository;
import org.estatio.fixture.EstatioBaseLineFixture;
import org.estatio.fixture.asset.PropertyForKalNl;
import org.estatio.fixture.asset.PropertyForOxfGb;
import org.estatio.fixture.charge.ChargeRefData;
import org.estatio.fixture.currency.CurrenciesRefData;
import org.estatio.fixture.invoice.InvoiceForLeaseItemTypeOfRentOneQuarterForKalPoison001;
import org.estatio.fixture.invoice.InvoiceForLeaseItemTypeOfRentOneQuarterForOxfPoison003;
import org.estatio.fixture.lease.LeaseForOxfPoison003Gb;
import org.estatio.fixture.lease.LeaseItemAndTermsForOxfPoison003Gb;
import org.estatio.fixture.party.OrganisationForHelloWorldGb;
import org.estatio.fixture.party.OrganisationForPoisonGb;
import org.estatio.fixture.security.tenancy.ApplicationTenancyForGb;
import org.estatio.fixture.security.tenancy.ApplicationTenancyForNl;
import org.estatio.integtests.EstatioIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

public class CommunicationChannelSubscriptionsTest extends EstatioIntegrationTest {

    @Inject
    InvoiceRepository invoiceRepository;

    @Inject
    NumeratorForCollectionMenu estatioNumeratorRepository;

    @Inject
    NumeratorRepository numeratorRepository;

    @Inject
    PartyRepository partyRepository;

    @Inject
    LeaseRepository leaseRepository;

    @Inject
    PropertyRepository propertyRepository;

    @Inject
    ApplicationTenancyRepository applicationTenancyRepository;

    @Inject
    BookmarkService bookmarkService;


    public static class FindInvoiceRepositoryTest extends CommunicationChannelSubscriptionsTest {

        @Before
        public void setupData() {
            runFixtureScript(new FixtureScript() {
                @Override
                protected void execute(ExecutionContext executionContext) {
                    executionContext.executeChild(this, new EstatioBaseLineFixture());

                    executionContext.executeChild(this, new InvoiceForLeaseItemTypeOfRentOneQuarterForOxfPoison003());
                    executionContext.executeChild(this, new InvoiceForLeaseItemTypeOfRentOneQuarterForKalPoison001());
                }
            });
        }

        private static String runId = "2014-02-16T02:30:03.156 - OXF - [OXF-TOPMODEL-001] - [RENT, SERVICE_CHARGE, TURNOVER_RENT, TAX] - 2012-01-01 - 2012-01-01/2012-01-02";

        Property propertyKal;

        Lease lease;

        Party buyer;

        Party seller;

        ApplicationTenancy applicationTenancy;

        @Before
        public void setUp() throws Exception {
            applicationTenancy = applicationTenancyRepository.findByPath(ApplicationTenancyForNl.PATH);
            seller = partyRepository.findPartyByReference(InvoiceForLeaseItemTypeOfRentOneQuarterForKalPoison001.PARTY_REF_SELLER);
            buyer = partyRepository.findPartyByReference(InvoiceForLeaseItemTypeOfRentOneQuarterForKalPoison001.PARTY_REF_BUYER);
            lease = leaseRepository.findLeaseByReference(InvoiceForLeaseItemTypeOfRentOneQuarterForKalPoison001.LEASE_REF);

            propertyKal = propertyRepository.findPropertyByReference(PropertyForKalNl.REF);

            Invoice invoice = invoiceRepository.findOrCreateMatchingInvoice(
                    applicationTenancy,
                    seller,
                    buyer,
                    PaymentMethod.DIRECT_DEBIT,
                    lease,
                    InvoiceStatus.NEW,
                    InvoiceForLeaseItemTypeOfRentOneQuarterForKalPoison001.startDateFor(lease),
                    null);
            invoice.setRunId(runId);
            Assert.assertNotNull(invoice);
        }

        public static class OnRemoveTest extends FindInvoiceRepositoryTest {

            @Inject
            RepositoryService repositoryService;
            CommunicationChannel originalChannel;
            CommunicationChannel replacementChannel;

            @Before
            public void lookupCommChannel() throws Exception {
                final List<CommunicationChannel> communicationChannels = repositoryService
                        .allInstances(CommunicationChannel.class);
                assertThat(communicationChannels.size()).isGreaterThanOrEqualTo(2);
                originalChannel = communicationChannels.get(0);
                replacementChannel = communicationChannels.get(1);
            }

            @Inject
            CommunicationChannelSubscriptions communicationChannelSubscriptions;

            @Test
            public void when_none_and_no_replacement() {

                // when
                wrap(originalChannel).remove(null);

                // then ok..

            }

            @Test
            public void when_some_and_replacement() {

                // given
                final List<Invoice> invoiceList = repositoryService.allInstances(Invoice.class);
                assertThat(invoiceList.size()).isGreaterThanOrEqualTo(2);
                final Invoice invoice0 = invoiceList.get(0);
                final Invoice invoice1 = invoiceList.get(1);

                invoice0.setSendTo(originalChannel);
                invoice1.setSendTo(replacementChannel);

                // when
                wrap(originalChannel).remove(replacementChannel);

                // then
                assertThat(invoice0.getSendTo()).isSameAs(replacementChannel);
            }

            @Test
            public void when_some_and_not_yet_invoiced_and_no_replacement() {

                // given
                final List<Invoice> invoiceList = repositoryService.allInstances(Invoice.class);
                assertThat(invoiceList.size()).isGreaterThanOrEqualTo(2);
                final Invoice invoice0 = invoiceList.get(0);
                final Invoice invoice1 = invoiceList.get(1);

                invoice0.setSendTo(originalChannel);
                invoice1.setSendTo(replacementChannel);

                // expect
                expectedExceptions.expect(InvalidException.class);
                expectedExceptions.expectMessage(containsString("Communication channel is being used (as the 'sendTo' channel for 1 invoice(s); provide a replacement"));

                // when
                wrap(originalChannel).remove(null);
            }


            @Test
            public void when_some_but_already_invoiced() {

                // given
                final List<Invoice> invoiceList = repositoryService.allInstances(Invoice.class);
                assertThat(invoiceList.size()).isGreaterThanOrEqualTo(2);
                final Invoice invoice0 = invoiceList.get(0);
                final Invoice invoice1 = invoiceList.get(1);

                invoice0.setSendTo(originalChannel);
                invoice1.setSendTo(replacementChannel);

                invoice0.setInvoiceNumber("XXX-000123");

                // when
                wrap(originalChannel).remove(null);

                // then
                assertThat(invoice0.getSendTo()).isNull();
            }
        }
    }

    public static class FindInvoiceRepositoryByRunId extends CommunicationChannelSubscriptionsTest {

        private Property kalProperty;

        @Before
        public void setupData() {
            runFixtureScript(new FixtureScript() {
                @Override
                protected void execute(ExecutionContext executionContext) {
                    executionContext.executeChild(this, new EstatioBaseLineFixture());

                    executionContext.executeChild(this, new InvoiceForLeaseItemTypeOfRentOneQuarterForOxfPoison003());
                    executionContext.executeChild(this, new InvoiceForLeaseItemTypeOfRentOneQuarterForKalPoison001());
                }
            });
        }

        private static String runId = "2014-02-16T02:30:03.156 - OXF - [OXF-TOPMODEL-001] - [RENT, SERVICE_CHARGE, TURNOVER_RENT, TAX] - 2012-01-01 - 2012-01-01/2012-01-02";

        @Before
        public void setUp() throws Exception {
            final ApplicationTenancy applicationTenancy = applicationTenancyRepository.findByPath(ApplicationTenancyForGb.PATH);
            final Party seller = partyRepository.findPartyByReference(InvoiceForLeaseItemTypeOfRentOneQuarterForOxfPoison003.PARTY_REF_SELLER);
            final Party buyer = partyRepository.findPartyByReference(InvoiceForLeaseItemTypeOfRentOneQuarterForOxfPoison003.PARTY_REF_BUYER);
            final Lease lease = leaseRepository.findLeaseByReference(InvoiceForLeaseItemTypeOfRentOneQuarterForOxfPoison003.LEASE_REF);
            final LocalDate startDate = InvoiceForLeaseItemTypeOfRentOneQuarterForOxfPoison003.startDateFor(lease);

            Invoice invoice = invoiceRepository.findOrCreateMatchingInvoice(
                    applicationTenancy,
                    seller, buyer, PaymentMethod.DIRECT_DEBIT, lease,
                    InvoiceStatus.NEW, startDate, null);
            invoice.setRunId(runId);
            Assert.assertNotNull(invoice);
        }

        @Test
        public void byRunId() {
            // when
            List<Invoice> result = invoiceRepository.findInvoicesByRunId(runId);

            // then
            assertThat(result.size(), is(1));
        }

    }

    @FixMethodOrder(MethodSorters.NAME_ASCENDING)
    public static class FindOrCreateMatchingInvoice extends CommunicationChannelSubscriptionsTest {

        @Before
        public void setupData() {
            runFixtureScript(new FixtureScript() {
                @Override
                protected void execute(ExecutionContext executionContext) {
                    executionContext.executeChild(this, new EstatioBaseLineFixture());

                    executionContext.executeChild(this, new LeaseItemAndTermsForOxfPoison003Gb());
                }
            });
        }

        private ApplicationTenancy applicationTenancy;
        private Party seller;
        private Party buyer;
        private Lease lease;
        private LocalDate invoiceStartDate;

        @Before
        public void setUp() throws Exception {
            applicationTenancy = applicationTenancyRepository.findByPath(ApplicationTenancyForGb.PATH);
            seller = partyRepository.findPartyByReference(OrganisationForHelloWorldGb.REF);
            buyer = partyRepository.findPartyByReference(OrganisationForPoisonGb.REF);
            lease = leaseRepository.findLeaseByReference(LeaseForOxfPoison003Gb.REF);
            invoiceStartDate = InvoiceForLeaseItemTypeOfRentOneQuarterForOxfPoison003.startDateFor(lease);
        }

        @Test
        public void whenDoesNotExist() {
            // given
            Assert.assertThat(invoiceRepository.allInvoices().isEmpty(), is(true));
            // when
            Invoice invoice = invoiceRepository.findOrCreateMatchingInvoice(
                    applicationTenancy,
                    seller, buyer, PaymentMethod.DIRECT_DEBIT, lease,
                    InvoiceStatus.NEW, invoiceStartDate, null);
            // then
            Assert.assertNotNull(invoice);
            Assert.assertThat(invoiceRepository.allInvoices().isEmpty(), is(false));
        }

        @Test
        public void whenExist() {
            // given
            Invoice invoice = invoiceRepository.findOrCreateMatchingInvoice(
                    applicationTenancy,
                    seller, buyer, PaymentMethod.DIRECT_DEBIT, lease,
                    InvoiceStatus.NEW, invoiceStartDate, null);
            // when
            Invoice invoice2 = invoiceRepository.findOrCreateMatchingInvoice(
                    applicationTenancy,
                    seller, buyer, PaymentMethod.DIRECT_DEBIT, lease,
                    InvoiceStatus.NEW, invoiceStartDate, null);
            // then
            Assert.assertThat(invoice2, is(sameInstance(invoice)));
        }

    }

    public static class InvoiceForLeaseImportTest extends CommunicationChannelSubscriptionsTest {

        @Before
        public void setup() {

            runFixtureScript(new FixtureScript() {
                @Override
                protected void execute(ExecutionContext executionContext) {
                    executionContext.executeChild(this, new EstatioBaseLineFixture());
                    executionContext.executeChild(this, new PropertyForOxfGb());
                    executionContext.executeChild(this, new LeaseItemAndTermsForOxfPoison003Gb());
                }
            });

        }

        String leaseReference;
        LocalDate dueDate;
        String paymentMethodStr;
        String itemDescription;
        String itemChargeReference;
        BigDecimal netAmount;
        LocalDate itemStartDate;
        LocalDate itemEndDate;

        @Test
        public void importInvoiceLine() throws Exception {

            // given
            leaseReference = LeaseForOxfPoison003Gb.REF;
            dueDate = new LocalDate(2016, 07, 01);
            itemStartDate = new LocalDate(2015, 01, 01);
            itemEndDate = new LocalDate(2015, 12, 31);
            paymentMethodStr = "DIRECT_DEBIT";
            itemChargeReference = ChargeRefData.IT_SERVICE_CHARGE;
            itemDescription = "Some description";
            netAmount = new BigDecimal("100.23");

            InvoiceImportLine invoiceImportLine = new InvoiceImportLine(
                    leaseReference,
                    dueDate,
                    paymentMethodStr,
                    itemChargeReference,
                    itemDescription,
                    netAmount,
                    itemStartDate,
                    itemEndDate);

            InvoiceImportLine invoiceImportLine2 = new InvoiceImportLine(
                    leaseReference,
                    dueDate,
                    paymentMethodStr,
                    itemChargeReference,
                    null,
                    netAmount,
                    itemStartDate,
                    itemEndDate);

            // when
            wrap(invoiceImportLine).importData();
            wrap(invoiceImportLine2).importData();

            // then

            // two import lines create to invoices
            assertThat(invoiceRepository.findByStatus(InvoiceStatus.NEW).size()).isEqualTo(2);

            // for first invoice
            Invoice invoice = invoiceRepository.findByStatus(InvoiceStatus.NEW).get(0);
            assertThat(invoice.getDueDate()).isEqualTo(dueDate);
            assertThat(invoice.getNetAmount()).isEqualTo(netAmount);
            assertThat(invoice.getCurrency().getReference()).isEqualTo(CurrenciesRefData.EUR);

            InvoiceItem item = invoice.getItems().first();
            assertThat(item.getDescription()).isEqualTo(itemDescription);
            assertThat(item.getStartDate()).isEqualTo(itemStartDate);
            assertThat(item.getEndDate()).isEqualTo(itemEndDate);
            assertThat(item.getCharge().getReference()).isEqualTo(itemChargeReference);
            assertThat(item.getQuantity()).isEqualTo(BigDecimal.ONE);

            // second invoice item defaults to charge description
            Invoice invoice2 = invoiceRepository.findByStatus(InvoiceStatus.NEW).get(1);
            InvoiceItem item2 = invoice2.getItems().first();
            assertThat(item2.getDescription()).isEqualTo(item2.getCharge().getDescription());

        }

    }

}