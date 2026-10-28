import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from '../header/header.component';
import { SidebarComponent } from '../sidebar/sidebar.component';

@Component({
  selector: 'app-admin-layout',
  imports: [RouterOutlet, HeaderComponent, SidebarComponent],
  templateUrl: './admin-layout.component.html',
})
export class AdminLayoutComponent {
  readonly mobileMenuOpen = signal(false);
}
