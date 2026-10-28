package com.devvinnii.clientmanager.api;

import com.devvinnii.clientmanager.api.model.Student;
import com.devvinnii.clientmanager.api.model.AppUser;
import com.devvinnii.clientmanager.api.model.Role;
import com.devvinnii.clientmanager.api.repository.AppUserRepository;
import com.devvinnii.clientmanager.api.repository.StudentRepository;
import com.devvinnii.clientmanager.api.service.StudentService;
import com.devvinnii.clientmanager.api.security.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GerenciadorDeAlunosApplicationTests {

	@Autowired
	private StudentRepository repository;

	@Autowired
	private StudentService service;

	@Autowired
	private AppUserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private MockMvc mockMvc;

	@AfterEach
	void cleanDatabase() {
		repository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void contextLoads() {
	}

	@Test
	void searchesStudentsByNameIgnoringCase() {
		repository.save(Student.builder().name("Ana Silva").cpf("11111111111").build());
		repository.save(Student.builder().name("Bruno Souza").cpf("22222222222").build());

		List<Student> students = service.searchByName("SILVA");

		assertThat(students)
				.extracting(Student::getName)
				.containsExactly("Ana Silva");
	}

	@Test
	void returnsStudentResponseDtoWithTheExistingFrontendFields() throws Exception {
		createUser("operador", "senha-segura", true, Role.OPERADOR);
		Student saved = repository.save(Student.builder()
				.name("Ana Silva")
				.email("ana@example.com")
				.cpf("33333333333")
				.phone("11999999999")
				.photoUrl("ana.png")
				.active(true)
				.build());

		mockMvc.perform(get("/api/clients/name/Ana").header("Authorization", bearer("operador")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").isNumber())
				.andExpect(jsonPath("$[0].name").value("Ana Silva"))
				.andExpect(jsonPath("$[0].email").value("ana@example.com"))
				.andExpect(jsonPath("$[0].cpf").value("33333333333"))
				.andExpect(jsonPath("$[0].phone").value("11999999999"))
				.andExpect(jsonPath("$[0].photoUrl").value("ana.png"));

		mockMvc.perform(get("/api/clients/" + saved.getId()).header("Authorization", bearer("operador")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(saved.getId()))
				.andExpect(jsonPath("$.active").value(true));
	}

	@Test
	void returnsStructuredFieldErrorsForInvalidRegistration() throws Exception {
		createUser("operador", "senha-segura", true, Role.OPERADOR);
		mockMvc.perform(multipart("/api/clients")
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.header("Authorization", bearer("operador"))
					.param("name", "")
					.param("email", "email-invalido")
					.param("cpf", "invalido"))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.status").value(422))
				.andExpect(jsonPath("$.message").value("Dados inválidos"))
				.andExpect(jsonPath("$.fieldErrors.name").exists())
				.andExpect(jsonPath("$.fieldErrors.email").exists())
				.andExpect(jsonPath("$.fieldErrors.cpf").exists());
	}

	@Test
	void logsInWithValidCredentials() throws Exception {
		createUser("admin", "senha-segura", true, Role.ADMIN);

		mockMvc.perform(post("/api/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"username\":\"admin\",\"password\":\"senha-segura\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andExpect(jsonPath("$.username").value("admin"))
				.andExpect(jsonPath("$.roles[0]").value("ADMIN"));
	}

	@Test
	void rejectsInvalidPassword() throws Exception {
		createUser("admin", "senha-segura", true, Role.ADMIN);

		mockMvc.perform(post("/api/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"username\":\"admin\",\"password\":\"senha-incorreta\"}"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("Credenciais inválidas"));
	}

	@Test
	void rejectsInactiveUserLogin() throws Exception {
		createUser("inativo", "senha-segura", false, Role.OPERADOR);

		mockMvc.perform(post("/api/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"username\":\"inativo\",\"password\":\"senha-segura\"}"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("Usuário inativo"));
	}

	@Test
	void rejectsRequestsWithoutToken() throws Exception {
		mockMvc.perform(get("/api/clients"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("Autenticação necessária ou token inválido"));
	}

	@Test
	void rejectsInvalidToken() throws Exception {
		mockMvc.perform(get("/api/clients").header("Authorization", "Bearer token-invalido"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("Autenticação necessária ou token inválido"));
	}

	@Test
	void allowsOperatorToManageStudentsAndReturnsAuthenticatedUser() throws Exception {
		createUser("operador", "senha-segura", true, Role.OPERADOR);

		mockMvc.perform(get("/api/auth/me").header("Authorization", bearer("operador")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("operador"))
				.andExpect(jsonPath("$.roles[0]").value("OPERADOR"));

		mockMvc.perform(get("/api/clients").header("Authorization", bearer("operador")))
				.andExpect(status().isOk());
	}

	@Test
	void allowsAdminToPerformAdministrativeStudentActions() throws Exception {
		createUser("admin", "senha-segura", true, Role.ADMIN);
		Student student = repository.save(Student.builder().name("Ana").cpf("52998224725").active(true).build());

		mockMvc.perform(patch("/api/clients/" + student.getId() + "/active")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"active\":false}")
					.header("Authorization", bearer("admin")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.active").value(false));

		mockMvc.perform(delete("/api/clients/" + student.getId()).header("Authorization", bearer("admin")))
				.andExpect(status().isOk());
	}

	@Test
	void allowsChiefOperationalAccessAndDeniesSensitiveAdministration() throws Exception {
		createUser("chefe", "senha-segura", true, Role.CHEFE);
		Student student = repository.save(Student.builder().name("Ana").cpf("52998224725").active(true).build());

		mockMvc.perform(get("/api/clients").header("Authorization", bearer("chefe")))
				.andExpect(status().isOk());
		mockMvc.perform(patch("/api/clients/" + student.getId() + "/active")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"active\":false}")
					.header("Authorization", bearer("chefe")))
				.andExpect(status().isForbidden());
	}

	@Test
	void allowsEmployeeStudentRegistrationAndDeniesSensitiveAdministration() throws Exception {
		createUser("funcionario", "senha-segura", true, Role.FUNCIONARIO);
		Student student = repository.save(Student.builder().name("Ana").cpf("52998224725").active(true).build());

		mockMvc.perform(multipart("/api/clients")
					.header("Authorization", bearer("funcionario"))
					.param("name", "Bruno")
					.param("cpf", "11144477735"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Bruno"));
		mockMvc.perform(delete("/api/clients/" + student.getId())
					.header("Authorization", bearer("funcionario")))
				.andExpect(status().isForbidden());
	}

	@Test
	void deniesOperatorAccessToStudentDeletion() throws Exception {
		createUser("operador", "senha-segura", true, Role.OPERADOR);

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/clients/1")
					.header("Authorization", bearer("operador")))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("Acesso negado"));
	}

	@Test
	void acceptsAndStoresValidPngPhotoWithSafeGeneratedName() throws Exception {
		createUser("operador", "senha-segura", true, Role.OPERADOR);
		MockMultipartFile photo = new MockMultipartFile("photo", "foto-da-ana.png", "image/png", validPng());

		mockMvc.perform(multipart("/api/clients")
					.file(photo)
					.header("Authorization", bearer("operador"))
					.param("name", "Ana Foto")
					.param("cpf", "52998224725"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.photoUrl").value(org.hamcrest.Matchers.matchesPattern("[a-f0-9-]+\\.png")));

		String storedName = repository.findByCpf("52998224725").orElseThrow().getPhotoUrl();
		mockMvc.perform(get("/api/clients/photos/" + storedName).header("Authorization", bearer("operador")))
				.andExpect(status().isOk())
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
						.string("X-Content-Type-Options", "nosniff"));
	}

	@Test
	void rejectsPhotoWithInvalidMimeType() throws Exception {
		createUser("operador", "senha-segura", true, Role.OPERADOR);
		MockMultipartFile photo = new MockMultipartFile("photo", "foto.png", "image/jpeg", validPng());

		performPhotoUpload(photo)
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.message").value("O tipo MIME da foto não corresponde à extensão permitida"));
	}

	@Test
	void rejectsPhotoWithMismatchedBinaryContent() throws Exception {
		createUser("operador", "senha-segura", true, Role.OPERADOR);
		MockMultipartFile photo = new MockMultipartFile("photo", "foto.jpg", "image/jpeg", validPng());

		performPhotoUpload(photo)
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.message").value("O conteúdo da foto não corresponde ao tipo informado"));
	}

	@Test
	void rejectsPhotoWithForbiddenExtensionOrPathTraversal() throws Exception {
		createUser("operador", "senha-segura", true, Role.OPERADOR);
		MockMultipartFile extension = new MockMultipartFile("photo", "foto.exe", "image/png", validPng());
		MockMultipartFile traversal = new MockMultipartFile("photo", "../foto.png", "image/png", validPng());

		performPhotoUpload(extension).andExpect(status().isUnprocessableEntity());
		performPhotoUpload(traversal).andExpect(status().isUnprocessableEntity());
	}

	@Test
	void rejectsPhotoAboveConfiguredMaximumSizeAndProtectsPhotoRoute() throws Exception {
		createUser("operador", "senha-segura", true, Role.OPERADOR);
		MockMultipartFile oversized = new MockMultipartFile("photo", "foto.png", "image/png", new byte[1025]);

		performPhotoUpload(oversized)
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.message").value("A foto excede o tamanho máximo permitido"));
		mockMvc.perform(get("/api/clients/photos/foto.png"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void updatesStudentAndPreservesPhotoWhenNoNewPhotoIsSent() throws Exception {
		createUser("operador", "senha-segura", true, Role.OPERADOR);
		Student student = repository.save(Student.builder().name("Ana").cpf("52998224725")
				.email("ana@ong.org").phone("11999999999").photoUrl("foto.png").active(true).build());

		mockMvc.perform(multipart("/api/clients/" + student.getId())
					.with(request -> {
						request.setMethod("PUT");
						return request;
					})
					.header("Authorization", bearer("operador"))
					.param("name", "Ana Atualizada")
					.param("email", "atualizada@ong.org")
					.param("cpf", "52998224725")
					.param("phone", "11888888888"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Ana Atualizada"))
				.andExpect(jsonPath("$.photoUrl").value("foto.png"));
	}

	@Test
	void rejectsInvalidCpfAndDuplicateCpf() throws Exception {
		createUser("operador", "senha-segura", true, Role.OPERADOR);
		mockMvc.perform(multipart("/api/clients")
					.header("Authorization", bearer("operador"))
					.param("name", "CPF inválido")
					.param("cpf", "11111111111"))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.fieldErrors.cpf").exists());

		repository.save(Student.builder().name("Existente").cpf("52998224725").active(true).build());
		mockMvc.perform(multipart("/api/clients")
					.header("Authorization", bearer("operador"))
					.param("name", "Duplicado")
					.param("cpf", "52998224725"))
				.andExpect(status().isConflict());
	}

	@Test
	void listsStudentsWithPaginationOrderingAndFilters() throws Exception {
		createUser("operador", "senha-segura", true, Role.OPERADOR);
		repository.save(Student.builder().name("Ana").cpf("52998224725").active(true).build());
		repository.save(Student.builder().name("Bruno").cpf("11144477735").active(true).build());
		repository.save(Student.builder().name("Carla").cpf("12345678909").active(false).build());

		mockMvc.perform(get("/api/clients/page")
					.header("Authorization", bearer("operador"))
					.param("active", "true")
					.param("page", "0")
					.param("size", "1")
					.param("sortBy", "name")
					.param("sortDirection", "DESC"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElements").value(2))
				.andExpect(jsonPath("$.content[0].name").value("Bruno"));
	}

	@Test
	void allowsOnlyAdminToChangeStatusAndDeleteStudent() throws Exception {
		createUser("operador", "senha-segura", true, Role.OPERADOR);
		createUser("admin", "senha-segura", true, Role.ADMIN);
		Student student = repository.save(Student.builder().name("Ana").cpf("52998224725").active(true).build());

		mockMvc.perform(patch("/api/clients/" + student.getId() + "/active")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"active\":false}")
					.header("Authorization", bearer("operador")))
				.andExpect(status().isForbidden());

		mockMvc.perform(patch("/api/clients/" + student.getId() + "/active")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"active\":false}")
					.header("Authorization", bearer("admin")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.active").value(false));

		mockMvc.perform(delete("/api/clients/" + student.getId()).header("Authorization", bearer("admin")))
				.andExpect(status().isOk());
		assertThat(repository.findById(student.getId())).isEmpty();
	}

	private org.springframework.test.web.servlet.ResultActions performPhotoUpload(MockMultipartFile photo) throws Exception {
		return mockMvc.perform(multipart("/api/clients")
				.file(photo)
				.header("Authorization", bearer("operador"))
				.param("name", "Aluno Foto")
				.param("cpf", "52998224725"));
	}

	private byte[] validPng() throws java.io.IOException {
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImageIO.write(image, "png", output);
		return output.toByteArray();
	}

	private void createUser(String username, String password, boolean active, Role role) {
		userRepository.save(AppUser.builder()
				.username(username)
				.passwordHash(passwordEncoder.encode(password))
				.active(active)
				.roles(Set.of(role))
				.build());
	}

	private String bearer(String username) {
		return "Bearer " + jwtService.generateToken(username, List.of());
	}

}
