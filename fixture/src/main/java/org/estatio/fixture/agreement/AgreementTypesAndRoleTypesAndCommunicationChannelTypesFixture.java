package org.estatio.fixture.agreement;


import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.fixtures.AbstractFixture;

import org.estatio.dom.agreement.AgreementRoleCommunicationChannelType;
import org.estatio.dom.agreement.AgreementRoleType;
import org.estatio.dom.agreement.AgreementType;
import org.estatio.dom.financial.BankMandate;
import org.estatio.dom.financial.FinancialConstants;
import org.estatio.dom.lease.LeaseConstants;

public class AgreementTypesAndRoleTypesAndCommunicationChannelTypesFixture extends AbstractFixture {

    @Override
    public void install() {
        create(FinancialConstants.AT_MANDATE, 
                new String[]{FinancialConstants.ART_CREDITOR, FinancialConstants.ART_DEBTOR, FinancialConstants.ART_OWNER},
                new String[]{FinancialConstants.ARCCT_BAR_ADDRESS, FinancialConstants.ARCCT_FOO_ADDRESS});
        create(LeaseConstants.AT_LEASE, 
                new String[]{LeaseConstants.ART_LANDLORD, LeaseConstants.ART_MANAGER, LeaseConstants.ART_TENANT},
                new String[]{LeaseConstants.ARCCT_ADMINISTRATION_ADDRESS, LeaseConstants.ARCCT_INVOICE_ADDRESS});
    }

    void create(final String atTitle, final String[] artTitles, final String[] arcctTitles) {
        AgreementType at = createAgreementType(atTitle, BankMandate.class.getName(), getContainer());
        for(String artTitle: artTitles) {
            createAgreementRoleType(artTitle, at, getContainer());
        }
        for(String arcctTitle: arcctTitles) {
            createAgreementRoleCommunicationChannelType(arcctTitle, at, getContainer());
        }
    }

    private static AgreementType createAgreementType(final String title, final String implementationClassName, final DomainObjectContainer container) {
        final AgreementType agreementType = container.newTransientInstance(AgreementType.class);
        agreementType.setTitle(title);
        agreementType.setImplementationClassName(implementationClassName);
        container.persist(agreementType);
        return agreementType;
    }
    
    private static AgreementRoleType createAgreementRoleType(final String title, final AgreementType appliesTo, final DomainObjectContainer container) {
        final AgreementRoleType agreementRoleType = container.newTransientInstance(AgreementRoleType.class);
        agreementRoleType.setTitle(title);
        agreementRoleType.setAppliesTo(appliesTo);
        container.persist(agreementRoleType);
        return agreementRoleType;
    }

    private static AgreementRoleCommunicationChannelType createAgreementRoleCommunicationChannelType(final String title, final AgreementType appliesTo, final DomainObjectContainer container) {
        final AgreementRoleCommunicationChannelType arcct = container.newTransientInstance(AgreementRoleCommunicationChannelType.class);
        arcct.setTitle(title);
        arcct.setAppliesTo(appliesTo);
        container.persist(arcct);
        return arcct;
    }
    


}