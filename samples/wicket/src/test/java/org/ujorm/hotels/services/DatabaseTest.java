/*
 ** Copyright 2013, Pavel Ponec
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
package org.ujorm.hotels.services;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.ujorm.Key;
import org.ujorm.criterion.Criterion;
import org.ujorm.hotels.entity.Booking;
import org.ujorm.hotels.entity.City;
import org.ujorm.hotels.entity.Hotel;
import org.ujorm.hotels.services.impl.AbstractServiceImpl;
import org.ujorm.orm.Query;
import static org.junit.Assert.*;

/**
 * Sample code for new article
 * @author Pavel Ponec
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:org/ujorm/hotels/config/applicationContext.xml"})
public class DatabaseTest extends AbstractServiceImpl {

    /** Database query using the Ujorm Keys */
    @Test
    @Transactional
    public void testDbQueries() {

        // Simple criterion:
        Key<Booking, Date> dateFrom = Booking.DATE_FROM;
        Criterion<Booking> crn1 = dateFrom.whereGt(now());
        List<Booking> futureAccommodations = crn1.evaluate(getBookings());
        assertEquals(1, futureAccommodations.size());

        // Composite keys:
        Key<Booking, Hotel> bookingHotel = Booking.HOTEL;
        Key<Hotel, City> hotelCity = Hotel.CITY;
        Key<City, String> cityName = City.NAME;
        Key<Booking, String> bookingCityName;
        bookingCityName = bookingHotel.add(hotelCity).add(cityName);
        assertEquals("HOTEL.CITY.NAME", bookingCityName.toString());

        // Building criterions:
        Criterion<Booking> crn2 = bookingCityName.whereEq("Prague");
        Criterion<Booking> crn3 = crn1.and(crn2);
        Criterion<Booking> crn4 = crn1.and(crn2.or(getNextCriterion()));
        assertEquals(1, crn4.evaluate(getBookings()).size());

        // Build query:
        Query<Booking> bookings = createQuery(crn3);
        List<Booking> result = bookings.list();
        assertTrue(result.isEmpty());

        // Fetch columns:
        bookings.addColumn(bookingHotel.add(hotelCity).add(City.ID));
        // Ordering:
        bookings.orderBy(Booking.DATE_FROM);
        bookings.orderBy(Booking.PRICE.descending());
    }

    // ---------- HELPFUL METHODS ----------

    /** Returns the current day */
    private Date now() {
        return new Date(System.currentTimeMillis());
    }

    /** Returns two Booking objects with different DateFrom attribute value */
    private List<Booking> getBookings() {
        List<Booking> result = new ArrayList<Booking>();
        int oneDay = 24 * 60 * 60 * 1000; // The one day in MILOS */

        Booking item1 = new Booking();
        result.add(item1);
        item1.setDateFrom(new Date(System.currentTimeMillis() - oneDay));

        Booking item2 = new Booking();
        result.add(item2);
        item2.setDateFrom(new Date(System.currentTimeMillis() + oneDay));

        return result;
    }

    /** Returns some next Criterion */
    private Criterion<Booking> getNextCriterion() {
        return Booking.CURRENCY.whereEq("USD");
    }
}