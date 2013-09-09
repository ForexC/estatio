/*
 *
 *  Copyright 2012-2013 Eurocommercial Properties NV
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
package org.estatio.dom.communicationchannel;

import org.estatio.dom.EstatioDomainService;

import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.NotInServiceMenu;

@Hidden
public class PhoneOrFaxNumbers extends EstatioDomainService<PhoneOrFaxNumber> {

    public PhoneOrFaxNumbers() {
        super(PhoneOrFaxNumbers.class, PhoneOrFaxNumber.class);
    }

    // //////////////////////////////////////

    @NotInServiceMenu
    public PhoneOrFaxNumber findByPhoneOrFaxNumber(final CommunicationChannelOwner owner, final String phoneNumber) {
        return firstMatch("findByPhoneNumber", "owner", owner, "phoneNumber", phoneNumber);
    }

}