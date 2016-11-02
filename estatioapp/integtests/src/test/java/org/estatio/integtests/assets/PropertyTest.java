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
package org.estatio.integtests.assets;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.wrapper.DisabledException;

import org.estatio.app.menus.asset.PropertyMenu;
import org.estatio.dom.asset.Property;
import org.estatio.dom.asset.PropertyRepository;
import org.estatio.dom.asset.Unit;
import org.estatio.dom.lease.Occupancy;
import org.estatio.dom.lease.OccupancyRepository;
import org.estatio.dom.lease.contributed.Property_vacantUnits;
import org.estatio.fixture.EstatioBaseLineFixture;
import org.estatio.fixture.EstatioFakeDataService;
import org.estatio.fixture.asset.PropertyBuilder;
import org.estatio.fixture.asset.PropertyForOxfGb;
import org.estatio.fixture.lease.LeaseForOxfTopModel001Gb;
import org.estatio.integtests.EstatioIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

public class PropertyTest extends EstatioIntegrationTest {

    @Before
    public void setupData() {
        runFixtureScript(new FixtureScript() {
            @Override
            protected void execute(ExecutionContext executionContext) {
                executionContext.executeChild(this, new EstatioBaseLineFixture());

                executionContext.executeChild(this, new PropertyForOxfGb());
                executionContext.executeChild(this, new LeaseForOxfTopModel001Gb());
            }
        });
    }

    @Inject
    PropertyMenu propertyMenu;
    @Inject
    PropertyRepository propertyRepository;
    @Inject
    OccupancyRepository occupancyRepository;

    public static class GetUnits extends PropertyTest {

        @Test
        public void whenReturnsInstance_thenCanTraverseUnits() throws Exception {
            // given
            Property property = propertyRepository.findPropertyByReference(PropertyForOxfGb.REF);

            // when
            Set<Unit> units = property.getUnits();

            // then
            assertThat(units).hasSize(25);
        }

        @Test
        public void occupiedUnits() throws Exception {
            // given
            Property property = propertyRepository.findPropertyByReference(PropertyForOxfGb.REF);

            Set<Unit> allUnits = property.getUnits();
            Set<Unit> occupiedUnits = occupancyRepository.findByProperty(property)
                    .stream()
                    .map(Occupancy::getUnit)
                    .collect(Collectors.toSet());

            assertThat(allUnits).hasSize(25);
            assertThat(occupiedUnits).isNotEmpty();

            // When
            List<Unit> vacantUnits = wrap(mixin(Property_vacantUnits.class, property)).$$();

            // Then
            assertThat(vacantUnits).isNotEmpty();
            assertThat(vacantUnits.size()).isEqualTo(allUnits.size() - occupiedUnits.size());
        }
    }

    public static class Dispose extends PropertyTest {

        private PropertyBuilder fs;

        @Before
        public void setupData() {
            fs = new PropertyBuilder();

            runFixtureScript(new FixtureScript() {
                @Override
                protected void execute(ExecutionContext executionContext) {
                    executionContext.executeChild(this, new EstatioBaseLineFixture());
                    executionContext.executeChild(this, fs);
                }
            });
        }

        @Inject
        private PropertyMenu propertyMenu;
        @Inject
        private ClockService clockService;
        @Inject
        EstatioFakeDataService fakeDataService;

        @Test
        public void happyCase() throws Exception {

            //
            // given
            //
            final Property property = fs.getProperty();
            assertThat(property.getDisposalDate()).isNull();

            //
            // when
            //
            final LocalDate disposalDate = clockService.now().plusDays(fakeDataService.values().anInt(10, 20));
            wrap(property).dispose(disposalDate);

            //
            // then
            //
            assertThat(property.getDisposalDate()).isEqualTo(disposalDate);
        }

        @Test
        public void whenAlreadyDisposed() throws Exception {

            //
            // given
            //
            final Property property = fs.getProperty();

            //
            // and given
            //
            final LocalDate disposalDate = clockService.now().plusDays(fakeDataService.values().anInt(10, 20));
            wrap(property).dispose(disposalDate);

            assertThat(property.getDisposalDate()).isEqualTo(disposalDate);

            //
            // expect
            //
            expectedExceptions.expect(DisabledException.class);
            expectedExceptions.expectMessage("already disposed");

            //
            // when
            //
            final LocalDate disposalDate2 = clockService.now().plusDays(fakeDataService.values().anInt(30, 40));
            wrap(property).dispose(disposalDate);

            //
            // then
            //
            assertThat(property.getDisposalDate()).isEqualTo(disposalDate);

        }

    }
}