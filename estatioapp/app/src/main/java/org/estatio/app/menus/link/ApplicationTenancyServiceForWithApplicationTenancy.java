
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
package org.estatio.app.menus.link;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;

import org.isisaddons.module.security.dom.tenancy.WithApplicationTenancy;

import org.incode.module.documents.dom.spi.ApplicationTenancyService;

import org.estatio.dom.appsettings.EstatioSettingsService;

@DomainService(nature = NatureOfService.DOMAIN, menuOrder = "100")
public class ApplicationTenancyServiceForWithApplicationTenancy implements ApplicationTenancyService {


    @javax.inject.Inject
    private EstatioSettingsService estatioSettingsService;

    @Override
    public String atPathFor(final Object domainObject) {
        if(domainObject instanceof WithApplicationTenancy) {
            final WithApplicationTenancy withApplicationTenancy = (WithApplicationTenancy) domainObject;
            return withApplicationTenancy.getApplicationTenancy().getPath();
        }
        return null;
    }
}
