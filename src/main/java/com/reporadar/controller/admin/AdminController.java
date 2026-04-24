package com.reporadar.controller.admin;

import com.reporadar.entity.*;
import com.reporadar.repository.AdministratorRepository;
import com.reporadar.repository.CategoryRepository;
import com.reporadar.repository.ProjectRepository;
import com.reporadar.repository.TechnologyRepository;
import com.reporadar.service.github.GitHubImportService;
import com.reporadar.service.github.GitLabImportService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller//le dice a spring que esta clase es un controlador web. spring la escanea al arracar y la registra para gestionar
           //peticiones http, a diferencia de restcontroller(que devuelve respuestas en formato JSON), controller devuelve
           //nombres de vistas thymeleaf
@RequestMapping("/admin")//prefijo base para rutas de este controlador, si mañana se añaden mas metodos, todos viven bajo
                         //"/admin" sin repetirlo
public class AdminController {

    private final ProjectRepository projectRepository;
    private final AdministratorRepository administratorRepository;
    private final CategoryRepository categoryRepository;
    private final TechnologyRepository technologyRepository;
    private final GitHubImportService gitHubImportService;
    private final GitLabImportService gitLabImportService;

    public AdminController(ProjectRepository projectRepository,AdministratorRepository administratorRepository,
                           CategoryRepository categoryRepository,TechnologyRepository technologyRepository,
                           GitHubImportService gitHubImportService,GitLabImportService gitLabImportService){
        this.projectRepository=projectRepository;
        this.administratorRepository=administratorRepository;
        this.categoryRepository=categoryRepository;
        this.technologyRepository=technologyRepository;
        this.gitLabImportService=gitLabImportService;
        this.gitHubImportService=gitHubImportService;
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
                                @RequestParam String source,
                                RedirectAttributes ra) {

        //envolvemos toda la logica en un bloque try-catch por si hay algun error en la importacion
        try {
            String email = SecurityContextHolder.getContext()
                    .getAuthentication().getName(); //por medio de securitycontext recuperamos el email del usuario logueado
            Administrator admin = administratorRepository.findByEmail(email)//gracias al repositorio de administradores, encontramos
                    //al admin correspondiente al email encontrado
                    .orElseThrow(() -> new RuntimeException("Admin no encontrado"));//si no se encuentra en la bd, se lanza excepcion

            if ("gitlab".equals(source)) {
                gitLabImportService.importFromUrl(url, admin.getId());
            } else {
                gitHubImportService.importFromUrl(url, admin.getId());
            }

            ra.addFlashAttribute("success", "Proyecto importado correctamente.");//se prepara mensaje de exito para
            //para mostrar en la siguiente pantalla
        } catch (Exception e) {
            ra.addFlashAttribute("error", "No se pudo importar: " + e.getMessage());
        }
        return "redirect:/admin/projects";//ordena al navegador a hacer una nueva peticion a la ruta /admin/projects, evitando asi que si el usuario
        //refresca la pagina, se vuelva a enviar el formulario
    }


    @GetMapping("/projects/{id}/edit")
    //este metodo muestra los datos del proyecto con x id
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes ra){
        Project project=projectRepository.findById(id).orElse(null);

        if(project==null){
            ra.addFlashAttribute("error","Proyecto no encontrado.");//si el proyecto no existe en la bd, se manda un mensaje
                                                                                            //flash de error, y redirige de vuelva a la lista de proyectos
            return "redirect:/admin/projects";
        }

        model.addAttribute("project",project);//pasamos el proyecto encontrado para que los campos del formulario se autorellenen
        model.addAttribute("allCategories",categoryRepository.findAll());//cargamos todas las categorias y tecnologias disponibles en la
                                                                                     //bd para que el usuario pueda elegirlas
        model.addAttribute("allTechnologies",technologyRepository.findAll());
        model.addAttribute("statuses", ProjectStatus.values());//pasamos todas los estados que puede tener un proyecto
        return"admin/projects/edit";
    }

    @PostMapping("/projects/{id}/edit")

    //este metodo procesa los datos enviamos por el formulario, y los guarda
    //RequestParam sirve para que las listas categoryids y techonolyids reciban una lista de todos los ids de las categorias y tecnologias, las cuales
    //son enviadas por el formulario.El required=false evita errores si el usuario desmarca todas las opciones(pues se enviaria un null)


    public String editProject(@PathVariable Long id, @RequestParam(required = false) List<Long>categoryIds,
                              @RequestParam (required = false) List<Long>technologyIds,@RequestParam ProjectStatus status,
                              RedirectAttributes ra){
        try{
            Project project=projectRepository.findById(id).orElseThrow(()-> new RuntimeException("Proyecto no encontrado"));
            project.setStatus(status);

            project.getCategories().clear();//cogemos todas las categorias(hibernate ya las tiene cargadas) y vaciamos el set, borrando las categorias
                                            //que tenian asignadas el proyecto
            //si el usuario no marca ningun checkbox, spring pasa null en lugar de una lista vacia, asi que convertimos nuestra lista a una vacia
            //para evitar que nada se rompa.Despues findAllById busca los ids de las categorias marcadas por el usuario. Por ultimo, con addAll
            //añadimos todas las categorias marcadas(o ninguna si no hay nada marcado)
            project.getCategories().addAll(categoryRepository.findAllById(categoryIds != null ? categoryIds : List.of()));

            //misma logica que con categorias
            project.getTechnologies().clear();
            project.getTechnologies().addAll(technologyRepository.findAllById(technologyIds != null ? technologyIds : List.of()));

            projectRepository.save(project);
            ra.addFlashAttribute("success","Proyecto actualizado correctamente");//mensaje flash de exito si todo va bien
        }catch(Exception e){
            ra.addFlashAttribute("error","No se pudo actualizar: " +e.getMessage());
        }
        return "redirect:/admin/projects";//le decimos al navegador que la peticion ya termino, que ahora se hara una nueva peticion a otra direccion
                                          //evitando que la misma peticion llegue mas de una vez si el usuario refresca la pagina con f5.
    }
}