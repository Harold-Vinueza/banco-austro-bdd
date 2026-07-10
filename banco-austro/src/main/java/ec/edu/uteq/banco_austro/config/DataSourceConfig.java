package ec.edu.uteq.banco_austro.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceConfig {

    @Value("${datasources.cuenca.url}")
    private String cuencaUrl;
    @Value("${datasources.cuenca.username}")
    private String cuencaUser;
    @Value("${datasources.cuenca.password}")
    private String cuencaPass;

    @Value("${datasources.quito.url}")
    private String quitoUrl;
    @Value("${datasources.quito.username}")
    private String quitoUser;
    @Value("${datasources.quito.password}")
    private String quitoPass;

    @Bean(name = "dsCuenca")
    public DataSource dsCuenca() {
        return DataSourceBuilder.create()
                .url(cuencaUrl)
                .username(cuencaUser)
                .password(cuencaPass)
                .driverClassName("org.postgresql.Driver")
                .build();
    }

    @Bean(name = "dsQuito")
    public DataSource dsQuito() {
        return DataSourceBuilder.create()
                .url(quitoUrl)
                .username(quitoUser)
                .password(quitoPass)
                .driverClassName("org.postgresql.Driver")
                .build();
    }
}