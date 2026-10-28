import { Injectable } from '@angular/core';
import { StoredSession } from '../../shared/models/auth.model';

const SESSION_KEY = 'talentos-capao.admin.session';

@Injectable({ providedIn: 'root' })
export class TokenStorageService {
  read(): StoredSession | null {
    try {
      const rawSession = sessionStorage.getItem(SESSION_KEY);
      if (!rawSession) {
        return null;
      }
      return JSON.parse(rawSession) as StoredSession;
    } catch {
      this.clear();
      return null;
    }
  }

  save(session: StoredSession): void {
    sessionStorage.setItem(SESSION_KEY, JSON.stringify(session));
  }

  clear(): void {
    sessionStorage.removeItem(SESSION_KEY);
  }
}
