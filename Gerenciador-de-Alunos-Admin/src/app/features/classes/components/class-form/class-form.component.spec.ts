import { ClassFormComponent } from './class-form.component';

describe('ClassFormComponent', () => {
  it('starts with one valid schedule and supports multiple schedules', () => {
    const component = new ClassFormComponent();
    component.ngOnInit();

    expect(component.schedules.length).toBe(1);
    component.addSchedule();
    expect(component.schedules.length).toBe(2);
    component.removeSchedule(1);
    expect(component.schedules.length).toBe(1);
  });

  it('does not submit when the end time is not after the start time', () => {
    const component = new ClassFormComponent();
    component.ngOnInit();
    component.form.patchValue({
      name: 'Natação Infantil',
      responsible: 'Professora Ana',
      maxCapacity: 12,
    });
    component.schedules.at(0).patchValue({ startTime: '10:00', endTime: '09:00' });
    spyOn(component.submitted, 'emit');

    component.submit();

    expect(component.hasInvalidTime()).toBeTrue();
    expect(component.submitted.emit).not.toHaveBeenCalled();
  });

  it('emits only an official modality with normalized form data', () => {
    const component = new ClassFormComponent();
    component.ngOnInit();
    component.form.patchValue({
      name: 'Ballet Juvenil',
      modality: 'BALLET',
      responsible: 'Professora Bia',
      maxCapacity: 15,
    });
    let emitted: unknown;
    component.submitted.subscribe((value) => (emitted = value));

    component.submit();

    expect(emitted).toEqual(
      jasmine.objectContaining({ name: 'Ballet Juvenil', modality: 'BALLET', maxCapacity: 15 }),
    );
  });
});
