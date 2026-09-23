package com.devvinnii.clientmanager.api;

import com.devvinnii.clientmanager.api.model.Client;
import com.devvinnii.clientmanager.api.model.AppUser;
import com.devvinnii.clientmanager.api.model.Role;
import com.devvinnii.clientmanager.api.repository.AppUserRepository;
import com.devvinnii.clientmanager.api.repository.ClientRepository;
import com.devvinnii.clientmanager.api.service.ClientService;
import com.devvinnii.clientmanager.api.security.JwtService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GerenciadorDeAlunosApplicationTests {

	@Autowired
	private ClientRepository repository;

	@Autowired
	private ClientService service;

	@Autowired
	private AppUserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private ObjectMapper objectMapper;

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
	void searchesClientsByNameIgnoringCase() {
		repository.save(Client.builder().name("Ana Silva").cpf("11111111111").build());
		repository.save(Client.builder().name("Bruno Souza").cpf("22222222222").build());

		List<Client> clients = service.searchByName("SILVA");

		assertThat(clients)
				.extracting(Client::getName)
				.containsExactly("Ana Silva");
	}

	@Test
	void returnsClientResponseDtoWithTheExistingFrontendFields() throws Exception {
		createUser("operador", "senha-segura", true, Role.OPERADOR);
		repository.save(Client.builder()
				.name("Ana Silva")
				.email("ana@example.com")
				.cpf("33333333333")
				.phone("11999999999")
				.photoUrl("ana.png")
				.build());

		mockMvc.perform(get("/api/clients/name/Ana").header("Authorization", bearer("operador")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").isNumber())
				.andExpect(jsonPath("$[0].name").value("Ana Silva"))
				.andExpect(jsonPath("$[0].email").value("ana@example.com"))
				.andExpect(jsonPath("$[0].cpf").value("33333333333"))
				.andExpect(jsonPath("$[0].phone").value("11999999999"))
				.andExpect(jsonPath("$[0].photoUrl").value("ana.png"));
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
	void allowsOperatorToManageClientsAndReturnsAuthenticatedUser() throws Exception {
		createUser("operador", "senha-segura", true, Role.OPERADOR);

		mockMvc.perform(get("/api/auth/me").header("Authorization", bearer("operador")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("operador"))
				.andExpect(jsonPath("$.roles[0]").value("OPERADOR"));

		mockMvc.perform(get("/api/clients").header("Authorization", bearer("operador")))
				.andExpect(status().isOk());
	}

	@Test
	void deniesOperatorAccessToClientDeletion() throws Exception {
		createUser("operador", "senha-segura", true, Role.OPERADOR);

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/clients/1")
					.header("Authorization", bearer("operador")))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("Acesso negado"));
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
