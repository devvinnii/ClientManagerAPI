import { Component, computed, EventEmitter, inject, Output } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { navigationItems } from '../../core/auth/navigation.config';
import { BrandMarkComponent } from '../../shared/components/brand-mark/brand-mark.component';
import { NavIconComponent } from '../../shared/components/nav-icon/nav-icon.component';

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive, BrandMarkComponent, NavIconComponent],
  templateUrl: './sidebar.component.html',
})
export class SidebarComponent {
  private readonly auth = inject(AuthService);

  @Output() navigate = new EventEmitter<void>();

  readonly visibleItems = computed(() =>
    navigationItems.filter((item) => item.implemented && this.auth.hasRole(item.roles)),
  );
}
