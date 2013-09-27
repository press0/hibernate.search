package net.timontech.hibernate.search;

import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.Version;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.jboss.logging.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CarTest {

	private static Logger logger = Logger.getLogger(CarTest.class);

	private Car[] testCars = { new Car("Shelby American", "GT 350", (short) 1967, "This is Tim's car!"),
			new Car("Chevrolet", "Bel Air", (short) 1957, "This is a true classic") };

	private SessionFactory hibernateSessionFactory;

	private Session hibernateSession;

	@Before
	public void setUp() throws Exception {
		BasicConfigurator.configure();
		Configuration configuration = new Configuration();
		configuration.configure("hibernate-test-cfg.xml");
		ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties())
				.buildServiceRegistry();
		hibernateSessionFactory = configuration.buildSessionFactory(serviceRegistry);
		hibernateSession = hibernateSessionFactory.openSession();
		populateDBWithTestData();
	}

	@Test
	public void testUsingLuceneQueryParserWithProjection() throws Exception {
		FullTextSession fullTestSession = Search.getFullTextSession(hibernateSession);
		// The second arg - the default field - specifies which
		// field to search if we don't specify the field in our query
		QueryParser parser = new QueryParser(Version.LUCENE_36, "model", new StandardAnalyzer(Version.LUCENE_36));
		String searchString = "model:" + "GT 350" + " OR model:" + "Bel Air";
		Query luceneQuery = parser.parse(searchString);
		FullTextQuery fullTextQuery = fullTestSession.createFullTextQuery(luceneQuery);
		/*
		 * Setting a projection avoids hitting the database and retrieving data
		 * not required by your search use case. This can save overhead when you
		 * only need to return a "read-only" list of items. If no project is
		 * set, Hibernate Search will fetch all matching Hibernate-managed
		 * entities from the database.
		 */
		fullTextQuery.setProjection("id", "model");
		List<Object[]> searchResults = (List<Object[]>) fullTextQuery.list();

		boolean foundShelby = false;
		boolean foundBelAir = false;
		for (Object[] result : searchResults) {
			logger.debug("Result found: " + result[0] + ", " + result[1]);
			if (result[1].equals("GT 350")) {
				foundShelby = true;
			} else if (result[1].equals("Bel Air")) {
				foundBelAir = true;
			}
		}
		Assert.assertEquals(2, searchResults.size());
		Assert.assertTrue(foundShelby && foundBelAir);
	}

	@Test
	public void testUsingHibernateSearchQueryBuilderReturningFullEntity() {
		FullTextSession fullTextSession = Search.getFullTextSession(hibernateSession);
		QueryBuilder queryBuilder = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(Car.class).get();
		org.apache.lucene.search.Query luceneQuery = queryBuilder.bool()
				.should(queryBuilder.keyword().onField("model").matching("GT 350").createQuery())
				.should(queryBuilder.keyword().onField("model").matching("Bel Air").createQuery()).createQuery();
		org.hibernate.Query hibernateQuery = fullTextSession.createFullTextQuery(luceneQuery, Car.class);
		List<Car> searchResults = hibernateQuery.list();
		boolean foundShelby = false;
		boolean foundBelAir = false;
		for (Car car : searchResults) {
			logger.debug("Car found, id=" + car.getId() + ", model=" + car.getModel());
			if (car.getModel().equals("GT 350")) {
				foundShelby = true;
			} else if (car.getModel().equals("Bel Air")) {
				foundBelAir = true;
			}
		}
		Assert.assertEquals(2, searchResults.size());
		Assert.assertTrue(foundShelby && foundBelAir);
	}

	@Test
	public void testUsingLuceneBooleanQueryReturningFullEntity() throws Exception {
		FullTextSession fullTextSession = Search.getFullTextSession(hibernateSession);

		BooleanQuery bq = new BooleanQuery();
		TermQuery gt350TermQuery = new TermQuery(new Term("model", "GT 350"));
		TermQuery belAirTermQuery = new TermQuery(new Term("model", "Bel Air"));
		bq.add(gt350TermQuery, BooleanClause.Occur.SHOULD);
		bq.add(belAirTermQuery, BooleanClause.Occur.SHOULD);
		Query q = new QueryParser(Version.LUCENE_36, "cs-method", new StandardAnalyzer(Version.LUCENE_36)).parse(bq
				.toString());

		org.hibernate.Query hibernateQuery = fullTextSession.createFullTextQuery(q, Car.class);
		List<Car> searchResults = hibernateQuery.list();

		boolean foundShelby = false;
		boolean foundBelAir = false;
		for (Car car : searchResults) {
			logger.debug("Car found, id=" + car.getId() + ", model=" + car.getModel());
			if (car.getModel().equals("GT 350")) {
				foundShelby = true;
			} else if (car.getModel().equals("Bel Air")) {
				foundBelAir = true;
			}
		}
		Assert.assertEquals(2, searchResults.size());
		Assert.assertTrue(foundShelby && foundBelAir);
	}

	@After
	public void tearDown() throws Exception {
		Transaction tx = hibernateSession.beginTransaction();
		for (Car car : testCars) {
			// hibernateSession.delete(car);
		}
		tx.commit();
		hibernateSession.close();
		hibernateSessionFactory.close();
	}

	private void populateDBWithTestData() {
		Transaction tx = hibernateSession.beginTransaction();

		hibernateSession.save(testCars[0]);
		hibernateSession.save(testCars[1]);

		tx.commit();
	}
}
