import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app';
import { provideHttpClient } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { importProvidersFrom } from '@angular/core';
import { FormsModule } from '@angular/forms';

bootstrapApplication(AppComponent, {
  providers: [
    provideHttpClient(),
    provideAnimations(),
    importProvidersFrom(FormsModule)
  ]
}).catch(err => console.error(err));
