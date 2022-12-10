/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.superbiz.moviefun;

import org.apache.openejb.config.DeploymentFilterable;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ejb.embeddable.EJBContainer;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MoviesTest {
    private static EJBContainer ejbContainer;
    private static MoviesRemote movies;

    @BeforeClass
    public static void setUp() throws Exception {
        final Properties props = new Properties();
        props.setProperty(DeploymentFilterable.CLASSPATH_INCLUDE, ".*arquillian-tomee-moviefun-example.*"); // arquillian-tomee excluded by default
        ejbContainer = EJBContainer.createEJBContainer(props);
        Object object = ejbContainer.getContext().lookup("java:global/arquillian-tomee-moviefun-example/Movies!org.superbiz.moviefun.MoviesRemote");

        assertTrue(object instanceof MoviesRemote);
        movies = (MoviesRemote) object;
    }

    @AfterClass
    public static void tearDown() {
        if (ejbContainer != null) {
            ejbContainer.close();
        }
    }

    @Before
    @After
    public void clean() {
        movies.clean();
    }

    @Test
    public void testShouldAddAMovie() throws Exception {
        Movie movie = new Movie();
        movie.setDirector("Michael Bay");
        movie.setGenre("Action");
        movie.setRating(9);
        movie.setTitle("Bad Boys");
        movie.setYear(1995);
        movies.addMovie(movie);

        assertEquals(1, movies.count());
        List<Movie> moviesFound = movies.findByTitle("Bad Boys");

        assertEquals(1, moviesFound.size());
        assertEquals("Michael Bay", moviesFound.get(0).getDirector());
        assertEquals("Action", moviesFound.get(0).getGenre());
        assertEquals(9, moviesFound.get(0).getRating());
        assertEquals("Bad Boys", moviesFound.get(0).getTitle());
        assertEquals(1995, moviesFound.get(0).getYear());
    }

}
