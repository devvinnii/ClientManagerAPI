import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';

@Component({
  selector: 'app-dashboard-page',
  imports: [RouterLink, PageHeaderComponent],
  templateUrl: './dashboard.page.html',
})
export class DashboardPage {}
