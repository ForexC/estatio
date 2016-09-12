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
package org.incode.module.documents.dom.mixins;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.SemanticsOf;

import org.incode.module.documents.dom.applicability.Binder;
import org.incode.module.documents.dom.docs.DocumentAbstract;
import org.incode.module.documents.dom.docs.DocumentTemplate;
import org.incode.module.documents.dom.docs.DocumentTemplateRepository;
import org.incode.module.documents.dom.links.PaperclipRepository;
import org.incode.module.documents.dom.services.ClassService;
import org.incode.module.documents.dom.services.DocumentNamingService;
import org.incode.module.documents.dom.spi.ApplicationTenancyService;
import org.incode.module.documents.dom.types.DocumentTypeRepository;

@Mixin
public class Object_createDocument<T> {

    public static enum Intent {
        PREVIEW,
        CREATE_AND_SAVE
    }

    //region > constructor
    protected final T domainObject;

    public Object_createDocument(final T domainObject) {
        this.domainObject = domainObject;
    }
    //endregion


    @Action(semantics = SemanticsOf.NON_IDEMPOTENT)
    @ActionLayout(contributed = Contributed.AS_ACTION)
    public Object $$(
            final DocumentTemplate template,
            @ParameterLayout(named = "Action")
            final Intent intent
            ) throws IOException {
        final String documentName = null;
        final String roleName = null;
        final Binder.Binding binding = template.newBinding(domainObject);
        if (intent == Intent.PREVIEW) {
            return template.preview(binding.getDataModel(), null);
        }
        final String documentNameToUse = documentNameOf(documentName, domainObject, template);
        final DocumentAbstract doc = template.render(binding.getDataModel(), documentNameToUse);
        for (Object o : binding.getAttachTo()) {
            if(paperclipRepository.canAttach(o)) {
                paperclipRepository.attach(doc, roleName, o);
            }
        }
        return doc;
    }

    public boolean hide$$() {
        return choices0$$().isEmpty();
    }

    /**
     * All templates which are applicable to the domain object's atPath, and which either be previewed or, if not previewable,
     * then can be created and attached to at least one domain object.
     */
    public List<DocumentTemplate> choices0$$() {
        return getDocumentTemplates();
    }

    /**
     * For the selected template, the list of actions available (based on the binding)
     */
    public List<Intent> choices1$$(final DocumentTemplate template) {
        if(template == null) {
            return Lists.newArrayList();
        }
        final Binder.Binding binding = template.newBinding(domainObject);
        return intentsFor(template, binding.getAttachTo());
    }

    private List<DocumentTemplate> getDocumentTemplates() {
        final List<DocumentTemplate> templates = Lists.newArrayList();

        final String atPath = getAtPath();
        if(atPath == null) {
            return templates;
        }

        final List<DocumentTemplate> templatesForPath =
                documentTemplateRepository.findByApplicableToAtPath(atPath);

        // REVIEW: this could probably be simplified ...
        for (DocumentTemplate template : templatesForPath) {
            final Binder binder = template.binderFor(domainObject);
            if(binder != null) {
                final Binder.Binding binding = binder.newBinding(template, domainObject);
                final List<Object> attachTo = binding.getAttachTo();
                if(!intentsFor(template, attachTo).isEmpty()) {
                    templates.add(template);
                }
            }
        }
        return templates;
    }

    private List<Intent> intentsFor(final DocumentTemplate template, final List<Object> attachTo) {
        final List<Intent> intents = Lists.newArrayList();
        if(template == null) {
            return intents;
        }
        if(template.getRenderingStrategy().isPreviewsToUrl()) {
            intents.add(Intent.PREVIEW);
        }
        // so long as at least one exists...
        if(attachTo.stream().filter(x -> paperclipRepository.canAttach(x)).findFirst().isPresent()) {
            intents.add(Intent.CREATE_AND_SAVE);
        }
        return intents;
    }

    /**
     * Delegates to {@link DocumentNamingService} to allow more sophisticated rules to be plugged in (eg substitute
     * for any invalid characters).
     */
    private String documentNameOf(
            final String documentName, final T domainObject, final DocumentTemplate template) {
        return documentNamingService.nameOf(documentName, domainObject, template);
    }



    String getAtPath() {
        return applicationTenancyServices.stream()
                .map(x -> x.atPathFor(domainObject))
                .filter(x -> x != null)
                .findFirst()
                .orElse(null);
    }

    //region > injected services

    @Inject
    List<ApplicationTenancyService> applicationTenancyServices;

    @Inject
    PaperclipRepository paperclipRepository;

    @javax.inject.Inject
    DocumentTypeRepository documentTypeRepository;
    
    @javax.inject.Inject
    DocumentTemplateRepository documentTemplateRepository;

    @javax.inject.Inject
    DocumentNamingService documentNamingService;

    @javax.inject.Inject
    ClassService classService;

    //endregion

}
