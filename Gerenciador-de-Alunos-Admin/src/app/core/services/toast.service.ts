import { Injectable, signal } from '@angular/core';

export type ToastType = 'success' | 'error';

export interface ToastMessage {
  id: number;
  type: ToastType;
  title: string;
  message?: string;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private nextId = 0;
  readonly messages = signal<ToastMessage[]>([]);

  success(title: string, message?: string): void {
    this.show('success', title, message);
  }

  error(title: string, message?: string): void {
    this.show('error', title, message);
  }

  remove(id: number): void {
    this.messages.update((messages) => messages.filter((message) => message.id !== id));
  }

  private show(type: ToastType, title: string, message?: string): void {
    const id = ++this.nextId;
    this.messages.update((messages) => [...messages, { id, type, title, message }]);
    setTimeout(() => this.remove(id), 5000);
  }
}
