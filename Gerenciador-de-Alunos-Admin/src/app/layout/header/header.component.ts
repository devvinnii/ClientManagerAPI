import { Component, DestroyRef, EventEmitter, inject, Output, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { navigationItems } from '../../core/auth/navigation.config';
import { BrandMarkComponent } from '../../shared/components/brand-mark/brand-mark.component';
import { roleLabels } from '../../shared/models/role.model';

@Component({
  selector: 'app-header',
  imports: [BrandMarkComponent],
  templateUrl: './header.component.html',
})
export class HeaderComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  @Output() menuToggle = new EventEmitter<void>();

  readonly user = this.auth.user;
  readonly currentSection = signal(this.resolveSection(this.router.url));

  constructor() {
    this.router.events
      .pipe(
        filter((event): event is NavigationEnd => event instanceof NavigationEnd),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((event) => this.currentSection.set(this.resolveSection(event.urlAfterRedirects)));
  }

  roleLabel(): string {
    const role = this.auth.primaryRole();
    return role ? roleLabels[role] : 'Sem perfil';
  }

  logout(): void {
    this.auth.logout();
  }

  private resolveSection(url: string): string {
    return navigationItems.find((item) => url.startsWith(item.route))?.label ?? 'Painel administrativo';
  }
}
