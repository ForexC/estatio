package org.estatio.appsettings;

import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.MemberOrder;

@Hidden
public class EstatioSettingsService extends ApplicationSettingsServiceAbstract<EstatioSetting> {

    public EstatioSettingsService() {
        super(EstatioSetting.class);
    }

    @MemberOrder(sequence = "1")
    public LocalDate fetchLastDueDate() {
        return fetchSetting().getLastDueDate();
    }

    public void updateLastDueDate(LocalDate d) {
        fetchSetting().setLastDueDate(d);
    }

}