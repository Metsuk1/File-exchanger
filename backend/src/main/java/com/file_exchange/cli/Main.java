package com.file_exchange.cli;

import com.file_exchange.controllers.FileController;
import com.file_exchange.controllers.UserController;
import com.file_exchange.db.DatabaseConfig;
import com.file_exchange.repository.FileRepository;
import com.file_exchange.repository.SharedLinkRepository;
import com.file_exchange.repository.UserRepository;
import com.file_exchange.server.CustomWebServer;
import com.file_exchange.server.RouteRegistry;
import com.file_exchange.services.FileService;
import com.file_exchange.services.UserService;
import com.file_exchange.storage.MinioInitializer;
import com.file_exchange.storage.MinioStorageService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;
import io.minio.MinioClient;
import java.lang.reflect.InvocationTargetException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        HikariDataSource hikariDataSource = null;
        try {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(dotenv.get("DATABASE_URL", "jdbc:postgresql://localhost:5432/file_exchange"));
            config.setUsername(dotenv.get("DATABASE_USER", "postgres"));
            config.setPassword(dotenv.get("DATABASE_PASSWORD", "postgres"));
            hikariDataSource = new HikariDataSource(config);
            DataSource dataSource = hikariDataSource;

            DatabaseConfig dbInitializer = new DatabaseConfig(dataSource);
            dbInitializer.initialize();

            // MinIO setup
            String minioEndpoint = dotenv.get("MINIO_ENDPOINT", "http://localhost:9000");
            String minioAccessKey = dotenv.get("MINIO_ACCESS_KEY", "minioadmin");
            String minioSecretKey = dotenv.get("MINIO_SECRET_KEY", "minioadmin");
            String minioBucket = dotenv.get("MINIO_BUCKET", "file-exchange");

            MinioClient minioClient = MinioClient.builder()
                    .endpoint(minioEndpoint)
                    .credentials(minioAccessKey, minioSecretKey)
                    .build();

            MinioInitializer minioInitializer = new MinioInitializer(minioClient, minioBucket);
            minioInitializer.initialize();

            MinioStorageService storageService = new MinioStorageService(minioClient, minioBucket);

            UserRepository userRepository = new UserRepository(dataSource);
            FileRepository fileRepository = new FileRepository(dataSource);
            SharedLinkRepository sharedLinkRepository = new SharedLinkRepository(dataSource);
            UserService userService = new UserService(userRepository);
            FileService fileService = new FileService(fileRepository, sharedLinkRepository, storageService);

            UserController userController = new UserController(userService);
            FileController fileController = new FileController(fileService);

            RouteRegistry registry = new RouteRegistry();
            registry.registerController(userController);
            registry.registerController(fileController);

            CustomWebServer virtualServer = new CustomWebServer(8080, 200, true, registry);
            virtualServer.start();
            log.info("CustomWebServer started on http://localhost:8080");

            HikariDataSource dsToClose = hikariDataSource;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Shutting down server...");
                virtualServer.stop();
                dsToClose.close();
            }));
            log.info("Server is running. Press Ctrl+C to stop.");
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            log.info("Server interrupted");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("CustomWebServer stopped with errors: {}", e.getMessage(), e);
            if (hikariDataSource != null) {
                hikariDataSource.close();
            }
        }
    }
}
