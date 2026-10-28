import { Component, EventEmitter, inject, Output } from '@angular/core';
import { AuthService } from '../../core/auth/auth.service';
import { BrandMarkComponent } from '../../shared/components/brand-mark/brand-mark.component';
import { roleLabels } from '../../shared/models/role.model';

@Component({
  selector: 'app-header',
  imports: [BrandMarkComponent],
  templateUrl: './header.component.html',
})
export class HeaderComponent {
  private readonly auth = inject(AuthService);

  @Output() menuToggle = new EventEmitter<void>();

  readonly user = this.auth.user;

  roleLabel(): string {
    const role = this.auth.primaryRole();
    return role ? roleLabels[role] : 'Sem perfil';
  }

  logout(): void {
    this.auth.logout();
  }
}
