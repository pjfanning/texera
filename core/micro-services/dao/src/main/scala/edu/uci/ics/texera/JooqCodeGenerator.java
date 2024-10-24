package edu.uci.ics.texera;


import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.Configuration;
import org.jooq.meta.jaxb.Jdbc;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This class is used to generate java classes representing the sql table in Texera database
 * These auto generated classes are essential for the connection between backend and database when using JOOQ library.
 * <p>
 * Every time the table in the Texera database changes, including creating, dropping and modifying the tables,
 * this class must be run to update the corresponding java classes.
 * <p>
 * Remember to change the username and password to your owns before you run this class.
 * <p>
 * The username, password and connection url is located in texera\core\conf\jdbc.conf
 * The configuration file is located in texera\core\conf\jooq-conf.xml
 */
public class JooqCodeGenerator {

    public static void main(String[] args) throws Exception {
        Path jooqXmlPath = Path.of("dao").resolve("src").resolve("main").resolve("resources").resolve("jooq-conf.xml");
        Configuration jooqConfig = GenerationTool.load(Files.newInputStream(jooqXmlPath));

        Jdbc jooqJdbcConfig = new Jdbc();
        jooqJdbcConfig.setDriver("com.mysql.cj.jdbc.Driver");
        jooqJdbcConfig.setUrl(DaoConfig.jdbcUrl());
        jooqJdbcConfig.setUsername(DaoConfig.jdbcUsername());
        jooqJdbcConfig.setPassword(DaoConfig.jdbcPassword());
        jooqConfig.setJdbc(jooqJdbcConfig);

        GenerationTool.generate(jooqConfig);
    }

}



