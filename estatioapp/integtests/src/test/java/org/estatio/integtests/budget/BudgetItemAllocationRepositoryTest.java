package org.estatio.integtests.budget;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.fixturescripts.FixtureScript;

import org.estatio.dom.asset.Property;
import org.estatio.dom.asset.PropertyRepository;
import org.estatio.dom.budgeting.allocation.BudgetItemAllocationRepository;
import org.estatio.dom.budgeting.allocation.PartitionItem;
import org.estatio.dom.budgeting.budget.Budget;
import org.estatio.dom.budgeting.budget.BudgetRepository;
import org.estatio.dom.budgeting.budgetitem.BudgetItem;
import org.estatio.dom.budgeting.keytable.KeyTable;
import org.estatio.dom.budgeting.keytable.KeyTableRepository;
import org.estatio.dom.charge.Charge;
import org.estatio.dom.charge.ChargeRepository;
import org.estatio.fixture.EstatioBaseLineFixture;
import org.estatio.fixture.asset.PropertyForOxfGb;
import org.estatio.fixture.budget.BudgetItemAllocationsForOxf;
import org.estatio.fixture.budget.BudgetsForOxf;
import org.estatio.fixture.charge.ChargeRefData;
import org.estatio.integtests.EstatioIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

public class BudgetItemAllocationRepositoryTest extends EstatioIntegrationTest {

    @Inject
    BudgetItemAllocationRepository budgetItemAllocationRepository;

    @Inject
    BudgetRepository budgetRepository;

    @Inject
    PropertyRepository propertyRepository;

    @Inject
    KeyTableRepository keytablesRepository;

    @Inject
    ChargeRepository chargeRepository;

    @Before
    public void setupData() {
        runFixtureScript(new FixtureScript() {
            @Override
            protected void execute(final ExecutionContext executionContext) {
                executionContext.executeChild(this, new EstatioBaseLineFixture());
                executionContext.executeChild(this, new BudgetItemAllocationsForOxf());
            }
        });
    }

    public static class validateNewBudgetItemAllocation extends BudgetItemAllocationRepositoryTest {

        @Test
        public void doubleBudgetItemAllocation() throws Exception {

            // given
            Property property = propertyRepository.findPropertyByReference(PropertyForOxfGb.REF);
            Budget budget = budgetRepository.findByPropertyAndStartDate(property, BudgetsForOxf.BUDGET_2015_START_DATE);
            PartitionItem partitionItem = budget.getItems().first().getPartitionItems().first();

            //when, then
            assertThat(budgetItemAllocationRepository
                    .validateNewPartitionItem(
                            partitionItem.getCharge(),
                            partitionItem.getKeyTable(),
                            partitionItem.getBudgetItem(),
                            null)
            ).isEqualTo("This schedule item already exists");

        }

    }

    public static class FindByBudgetItemAllocation extends BudgetItemAllocationRepositoryTest {

        @Test
        public void happyCase() throws Exception {
            // given
            Property property = propertyRepository.findPropertyByReference(PropertyForOxfGb.REF);
            Budget budget = budgetRepository.findByPropertyAndStartDate(property, BudgetsForOxf.BUDGET_2015_START_DATE);
            BudgetItem budgetItem = budget.getItems().last();
            // when
            final List<PartitionItem> partitionItemList = budgetItemAllocationRepository.findByBudgetItem(budgetItem);
            // then
            assertThat(partitionItemList.size()).isEqualTo(2);

        }

    }

    public static class FindByKeyTable extends BudgetItemAllocationRepositoryTest {

        @Test
        public void happyCase() throws Exception {
            // given
            Property property = propertyRepository.findPropertyByReference(PropertyForOxfGb.REF);
            Budget budget = budgetRepository.findByPropertyAndStartDate(property, BudgetsForOxf.BUDGET_2015_START_DATE);
            KeyTable keyTable = keytablesRepository.findByBudget(budget).get(0);
            // when
            final List<PartitionItem> partitionItemList = budgetItemAllocationRepository.findByKeyTable(keyTable);
            // then
            assertThat(partitionItemList.size()).isEqualTo(2);

        }

    }

    public static class FindByBudgetItemAllocationAndKeyTable extends BudgetItemAllocationRepositoryTest {

        @Test
        public void happyCase() throws Exception {
            // given
            Property property = propertyRepository.findPropertyByReference(PropertyForOxfGb.REF);

            Budget budget = budgetRepository.findByPropertyAndStartDate(property, BudgetsForOxf.BUDGET_2015_START_DATE);
            BudgetItem budgetItem = budget.getItems().first();
            KeyTable keyTable = keytablesRepository.findByBudget(budget).get(0);
            Charge charge = chargeRepository.findByReference(ChargeRefData.GB_SERVICE_CHARGE);
            // when
            final PartitionItem partitionItem = budgetItemAllocationRepository.findByChargeAndBudgetItemAndKeyTable(charge, budgetItem, keyTable);
            // then
            assertThat(partitionItem.getBudgetItem()).isEqualTo(budgetItem);
            assertThat(partitionItem.getKeyTable()).isEqualTo(keyTable);
        }

    }

    public static class UpdateOrCreateAllocation extends BudgetItemAllocationRepositoryTest {

        PartitionItem partitionItem;
        BigDecimal origPercentage;
        BigDecimal newPercentage;


        @Test
        public void happyCase() throws Exception {
            // given
            Property property = propertyRepository.findPropertyByReference(PropertyForOxfGb.REF);

            Budget budget = budgetRepository.findByPropertyAndStartDate(property, BudgetsForOxf.BUDGET_2015_START_DATE);
            BudgetItem budgetItem = budget.getItems().first();
            KeyTable keyTable = keytablesRepository.findByBudget(budget).get(0);
            Charge charge = chargeRepository.findByReference(ChargeRefData.GB_SERVICE_CHARGE);

            origPercentage = new BigDecimal("100").setScale(6, BigDecimal.ROUND_HALF_UP);
            newPercentage = new BigDecimal("90").setScale(6, BigDecimal.ROUND_HALF_UP);

            partitionItem = budgetItemAllocationRepository.findByChargeAndBudgetItemAndKeyTable(charge, budgetItem, keyTable);
            assertThat(partitionItem.getPercentage()).isEqualTo(origPercentage);

            // when
            partitionItem = budgetItemAllocationRepository.updateOrCreatePartitionItem(budgetItem, charge, keyTable, newPercentage);

            // then
            assertThat(partitionItem.getBudgetItem()).isEqualTo(budgetItem);
            assertThat(partitionItem.getKeyTable()).isEqualTo(keyTable);
            assertThat(partitionItem.getPercentage()).isEqualTo(newPercentage);
        }

    }

}
