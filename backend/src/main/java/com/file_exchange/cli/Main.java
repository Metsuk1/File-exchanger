package com.file_exchange.cli;


import com.file_exchange.controllers.FileController;
import com.file_exchange.controllers.UserController;
import com.file_exchange.db.DatabaseInitializer;
import com.file_exchange.repository.FileRepository;
import com.file_exchange.repository.UserRepository;
import com.file_exchange.server.CustomWebServer;
import com.file_exchange.services.FileService;
import com.file_exchange.services.UserService;

import java.lang.reflect.InvocationTargetException;

public class Main {
    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        try {
            DatabaseInitializer dbInitializer = new DatabaseInitializer();
            dbInitializer.initialize();

            UserRepository userRepository = new UserRepository();
            FileRepository fileRepository = new FileRepository();
            UserService userService = new UserService(userRepository);
            FileService fileService = new FileService(fileRepository);

            UserController userController = new UserController(userService);
            FileController fileController = new FileController(fileService);

            CustomWebServer virtualServer = new CustomWebServer(8080, 200, true);
            virtualServer.registerController(userController);
            virtualServer.registerController(fileController);

            virtualServer.start();
            System.out.println("CustomWebServer started on http://localhost:8080");
            System.out.println("Press Enter to stop...");
            System.in.read();
            virtualServer.stop();
        } catch (Exception e) {
            System.out.println("CustomWebServer stopped with errors: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
