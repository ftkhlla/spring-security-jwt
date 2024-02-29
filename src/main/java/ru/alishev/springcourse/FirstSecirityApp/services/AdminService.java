package ru.alishev.springcourse.FirstSecirityApp.services;

import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    @RolesAllowed("ROLE_ADMIN")
    public void doAdminStuff(){
        System.out.println("Only admin here");
    }
}
