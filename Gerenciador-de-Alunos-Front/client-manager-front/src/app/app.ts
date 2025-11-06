import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrls: ['./app.css'],
})
export class AppComponent {
  clientes: any[] = [];
  clientesFiltrados: any[] = [];
  novoCliente = { name: '', cpf: '', email: '' };
  filtro: string = '';
  mensagemErro: string | null = null;
  mensagemSucesso: string | null = null;
  private apiUrl = 'http://localhost:8080/api/clients';

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.listarClientes();
  }

  listarClientes() {
    this.http.get<any[]>(this.apiUrl).subscribe((data) => {
      this.clientes = data;
      this.clientesFiltrados = data;
    });
  }

  cadastrarCliente() {
    this.mensagemErro = null;
    this.mensagemSucesso = null;

    const formData = new FormData();
    formData.append('name', this.novoCliente.name);
    formData.append('cpf', this.novoCliente.cpf);
    formData.append('email', this.novoCliente.email);

    this.http.post(this.apiUrl, formData).subscribe({
      next: () => {
        this.novoCliente = { name: '', cpf: '', email: '' };
        this.mensagemSucesso = 'Cliente cadastrado com sucesso!';
        this.listarClientes();
      },
      error: (err) => {
        console.error('Erro ao cadastrar cliente:', err);

        if (err.status === 409) {
          this.mensagemErro = 'Erro: CPF já cadastrado.';
        } else if (err.status === 422 && err.error?.message) {
          this.mensagemErro = 'Erro de validação: ' + err.error.message;
        } else {
          this.mensagemErro = 'Ocorreu um erro inesperado. Tente novamente.';
        }
      }
    });
  }

  deletarCliente(id: number) {
    this.http.delete(`${this.apiUrl}/${id}`).subscribe(() => {
      this.listarClientes();
    });
  }

  filtrarClientes() {
    const filtro = this.filtro.trim().toLowerCase();
    this.clientesFiltrados = this.clientes.filter(c =>
      c.name.toLowerCase().includes(filtro) || c.cpf.includes(filtro)
    );
  }
}
