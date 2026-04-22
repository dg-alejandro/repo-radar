package com.reporadar.controller.admin;

import com.reporadar.entity.Administrator;
import com.reporadar.entity.Project;
import com.reporadar.repository.AdministratorRepository;
import com.reporadar.repository.ProjectRepository;
import com.reporadar.service.github.GitHubImportService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller//le dice a spring que esta clase es un controlador web. spring la escanea al arracar y la registra para gestionar
           //peticiones http, a diferencia de restcontroller(que devuelve respuestas en formato JSON), controller devuelve
           //nombres de vistas thymeleaf
@RequestMapping("/admin")//prefijo base para rutas de este controlador, si mañana se añaden mas metodos, todos viven bajo
                         //"/admin" sin repetirlo
public class AdminController {

    private final ProjectRepository projectRepository;
    private final GitHubImportService gitHubImportService;
    private final AdministratorRepository administratorRepository;

    public AdminController(ProjectRepository projectRepository,GitHubImportService gitHubImportService,
                           AdministratorRepository administratorRepository){
        this.projectRepository=projectRepository;
        this.gitHubImportService=gitHubImportService;
        this.administratorRepository=administratorRepository;
    }

    @GetMapping("/login")//registra este metodo para responder a la ruta get /admin/login
    public String loginPage() {
        return "admin/login"; //apunta a templates/admin/login.html
    }

    @GetMapping("/projects")//se responde a la ruta /admin/projects
    public String projectList(Model model) {//model es un contenedor que spring inyecta automaticamente, sirve para pasar datos a la vista thymeleaf
        List<Project> projects = projectRepository.findAll();//llama al repositorio JPA para obtener todos los proyectos de la bd
        model.addAttribute("projects", projects);//mete la lista con la clave projects, para que la plantilla thymeleaf
                                                             //pueda acceder a ella con: th:each="project : ${projects}"
        return "admin/projects/list";
    }

    @GetMapping("/projects/import")//se responde a la ruta /admin/projects/import
    public String importForm() {
        return "admin/projects/import";//le dice a spring que busque el archivo html en dicha ruta y lo renderice
    }

    @PostMapping("/projects/import")//define que el metodo siguiente responde a peticiones POST(enviamos datos de formulario)

    //requestparam captura el valor del campo url que viene del html
    //ra sirve para pasar mensajes a la siguiente pagina despues de una redireccion
    public String importProject(@RequestParam String url,
                                RedirectAttributes ra) {

        //envolvemos toda la logica en un bloque try-catch por si hay algun error en la importacion
        try {
            String email = SecurityContextHolder.getContext()
                    .getAuthentication().getName(); //por medio de securitycontext recuperamos el email del usuario logueado
            Administrator admin = administratorRepository.findByEmail(email)//gracias al repositorio de administradores, encontramos
                                                                            //al admin correspondiente al email encontrado
                    .orElseThrow(() -> new RuntimeException("Admin no encontrado"));//si no se encuentra en la bd, se lanza excepcion
            gitHubImportService.importFromUrl(url, admin.getId());
            ra.addFlashAttribute("success", "Proyecto importado correctamente.");//se prepara mensaje de exito para
                                                                                                         //para mostrar en la siguiente pantalla
        } catch (Exception e) {
            ra.addFlashAttribute("error", "No se pudo importar: " + e.getMessage());
        }
        return "redirect:/admin/projects";//ordena al navegador a hacer una nueva peticion a la ruta /admin/projects, evitando asi que si el usuario
                                          //refresca la pagina, se vuelva a enviar el formulario
    }
}