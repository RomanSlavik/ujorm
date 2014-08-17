/*
 * Copyright 2013-2014, Pavel Ponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.hotels.gui.params;

import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.ujorm.criterion.Criterion;
import org.ujorm.hotels.entity.Hotel;
import org.ujorm.hotels.entity.ParamKey;
import org.ujorm.hotels.entity.ParamValue;
import org.ujorm.hotels.gui.params.action.ParamFinder;
import org.ujorm.hotels.services.AuthService;
import org.ujorm.hotels.services.DbService;
import org.ujorm.hotels.services.ParamService;
import org.ujorm.wicket.UjoEvent;
import org.ujorm.wicket.component.grid.ListDataProvider;
import static org.ujorm.wicket.component.grid.AbstractDataProvider.DEFAULT_DATATABLE_ID;


/**
 * ParamValue Panel
 * @author Pavel Ponec
 */
public class ParamsTable<U extends ParamValue> extends GenericPanel<U> {

    @SpringBean private DbService dbService;
    @SpringBean private AuthService authService;
    @SpringBean private ParamService paramService;

    private ParamFinder<U> toolbar = new ParamFinder("paramFinder");
    private ParamsEditor editDialog;

    public ParamsTable(String id) {
        super(id);

        ListDataProvider<U> columns = ListDataProvider.of(getCriterion());
        columns.add(ParamValue.KEY_MODULE$);
        columns.add(ParamValue.KEY_NAME$);
        columns.add(ParamValue.KEY_SYSTEM$);
        columns.add(ParamValue.PARAM_KEY.add(ParamKey.CLASS_NAME));
        columns.add(ParamValue.TEXT_VALUE);
        columns.add(ParamValue.PARAM_KEY.add(ParamKey.LAST_UPDATE));
        //columns.add(newActionColumn(ParamValue.ID));
        add(columns.createDataTable(20));
        columns.setRows(paramService.getValues(authService.getLoggedCustomer()));

        // Dialogs:
        add((editDialog = ParamsEditor.create("editDialog", 700, 390)).getModalWindow());
        add(toolbar);
    }

    /** Create a criterion for the table */
    @SuppressWarnings("unchecked")
    private IModel<Criterion<U>> getCriterion() {
        return authService.isLogged()
            ? toolbar.getCriterion()
            : toolbar.getCriterion();
    }

    /** Manage events */
    @Override
    public void onEvent(IEvent<?> argEvent) {
        final UjoEvent<Hotel> event = UjoEvent.get(argEvent);
        if (event != null) {
            if (event.isAction(ParamFinder.FILTER_ACTION)) {
                reloadTable(event);
            }
        }
    }

    /** Reload the data table */
    private void reloadTable(UjoEvent event) {
        event.addTarget(get(DEFAULT_DATATABLE_ID));
    }

}