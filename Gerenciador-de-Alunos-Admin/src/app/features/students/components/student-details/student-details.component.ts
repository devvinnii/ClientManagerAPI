import { Component, EventEmitter, HostListener, Input, Output } from '@angular/core';
import { Student } from '../../models/student.model';
import { CpfPipe } from '../../pipes/cpf.pipe';
import { PhonePipe } from '../../pipes/phone.pipe';
import { StudentAvatarComponent } from '../student-avatar/student-avatar.component';

@Component({
  selector: 'app-student-details',
  imports: [StudentAvatarComponent, CpfPipe, PhonePipe],
  templateUrl: './student-details.component.html',
})
export class StudentDetailsComponent {
  @Input({ required: true }) student!: Student;
  @Output() closed = new EventEmitter<void>();
  @Output() edit = new EventEmitter<Student>();

  @HostListener('document:keydown.escape')
  closeOnEscape(): void {
    this.closed.emit();
  }
}
