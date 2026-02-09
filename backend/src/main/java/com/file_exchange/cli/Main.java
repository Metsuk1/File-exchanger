package com.file_exchange.cli;

import com.file_exchange.controllers.FileController;
import com.file_exchange.controllers.UserController;
import com.file_exchange.db.DatabaseInitializer;
import com.file_exchange.repository.FileRepository;
import com.file_exchange.repository.SharedLinkRepository;
import com.file_exchange.repository.UserRepository;
import com.file_exchange.server.CustomWebServer;
import com.file_exchange.services.FileService;
import com.file_exchange.services.UserService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;
import java.lang.reflect.InvocationTargetException;
import javax.sql.DataSource;

public class Main {
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

            DatabaseInitializer dbInitializer = new DatabaseInitializer(dataSource);
            dbInitializer.initialize();

            UserRepository userRepository = new UserRepository(dataSource);
            FileRepository fileRepository = new FileRepository(dataSource);
            SharedLinkRepository sharedLinkRepository = new SharedLinkRepository(dataSource);
            UserService userService = new UserService(userRepository);
            FileService fileService = new FileService(fileRepository, sharedLinkRepository);

            UserController userController = new UserController(userService);
            FileController fileController = new FileController(fileService);

            CustomWebServer virtualServer = new CustomWebServer(8080, 200, true);
            virtualServer.registerController(userController);
            virtualServer.registerController(fileController);

            virtualServer.start();
            System.out.println("CustomWebServer started on http://localhost:8080");

            HikariDataSource dsToClose = hikariDataSource;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down server...");
                virtualServer.stop();
                dsToClose.close();
            }));
            System.out.println("Server is running. Press Ctrl+C to stop.");
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println("Server interrupted");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.out.println("CustomWebServer stopped with errors: " + e.getMessage());
            e.printStackTrace();
            if (hikariDataSource != null) {
                hikariDataSource.close();
            }
        }
    }
}
