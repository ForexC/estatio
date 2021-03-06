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
package org.estatio.dom.invoice.viewmodel.dnc;

import java.util.List;

import javax.inject.Inject;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import org.apache.isis.applib.services.factory.FactoryService;

import org.incode.module.communications.dom.impl.commchannel.CommunicationChannel;
import org.incode.module.communications.dom.impl.commchannel.CommunicationChannelType;
import org.incode.module.document.dom.impl.docs.Document;
import org.incode.module.document.dom.impl.docs.DocumentState;

import org.estatio.dom.invoice.Invoice;
import org.estatio.dom.invoice.viewmodel.InvoiceSummaryForPropertyDueDateStatus;

public abstract class InvoiceSummaryForPropertyDueDateStatus_sendAbstract extends InvoiceSummaryForPropertyDueDateStatus_actionAbstract {

    private final CommunicationChannelType communicationChannelType;

    public InvoiceSummaryForPropertyDueDateStatus_sendAbstract(
            final InvoiceSummaryForPropertyDueDateStatus invoiceSummary,
            final String documentTypeReference,
            final CommunicationChannelType communicationChannelType) {
        super(invoiceSummary, documentTypeReference);
        this.communicationChannelType = communicationChannelType;
    }


    abstract List<Document> documentsToSend();

    List<Document> documentsToSend(Predicate<Document> filter) {
        List<Document> documents = Lists.newArrayList();

        final List<Invoice> invoices = invoiceSummary.getInvoices();
        for (Invoice invoice : invoices) {

            final CommunicationChannel sendTo = invoice.getSendTo();
            if (sendTo == null) {
                continue;
            }

            final CommunicationChannelType channelType = sendTo.getType();
            if (channelType != communicationChannelType) {
                continue;
            }

            final Document document = findMostRecentAttachedTo(invoice, getDocumentType());
            if(document == null) {
                continue;
            }
            if(document.getState() == DocumentState.NOT_RENDERED) {
                continue;
            }

            if(!filter.apply(document)) {
                continue;
            }
            documents.add(document);
        }
        return documents;
    }


    @Inject
    FactoryService factoryService;
}
