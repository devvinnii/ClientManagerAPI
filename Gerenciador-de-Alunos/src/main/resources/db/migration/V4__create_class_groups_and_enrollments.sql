CREATE TABLE class_group (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    modality VARCHAR(30) NOT NULL,
    description TEXT,
    responsible VARCHAR(150) NOT NULL,
    max_capacity INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_class_group_modality CHECK (modality IN ('NATACAO', 'TRIATHLON', 'BALLET')),
    CONSTRAINT chk_class_group_capacity CHECK (max_capacity > 0)
);

CREATE INDEX idx_class_group_name ON class_group (name);
CREATE INDEX idx_class_group_modality_active ON class_group (modality, active);

CREATE TABLE class_schedule (
    id BIGINT NOT NULL AUTO_INCREMENT,
    class_group_id BIGINT NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_class_schedule_group FOREIGN KEY (class_group_id) REFERENCES class_group (id),
    CONSTRAINT uk_class_schedule_interval UNIQUE (class_group_id, day_of_week, start_time, end_time),
    CONSTRAINT chk_class_schedule_day CHECK (day_of_week IN ('SEGUNDA', 'TERCA', 'QUARTA', 'QUINTA', 'SEXTA', 'SABADO', 'DOMINGO')),
    CONSTRAINT chk_class_schedule_time CHECK (end_time > start_time)
);

CREATE INDEX idx_class_schedule_group ON class_schedule (class_group_id);

CREATE TABLE enrollment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    class_group_id BIGINT NOT NULL,
    enrolled_at TIMESTAMP(6) NOT NULL,
    status VARCHAR(20) NOT NULL,
    cancelled_at TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_enrollment_student FOREIGN KEY (student_id) REFERENCES client (id),
    CONSTRAINT fk_enrollment_group FOREIGN KEY (class_group_id) REFERENCES class_group (id),
    CONSTRAINT chk_enrollment_status CHECK (status IN ('ATIVA', 'CANCELADA')),
    CONSTRAINT chk_enrollment_cancelled_at CHECK (
        (status = 'ATIVA' AND cancelled_at IS NULL) OR
        (status = 'CANCELADA' AND cancelled_at IS NOT NULL)
    )
);

CREATE INDEX idx_enrollment_group_status ON enrollment (class_group_id, status);
CREATE INDEX idx_enrollment_student_status ON enrollment (student_id, status);
CREATE INDEX idx_enrollment_group_student ON enrollment (class_group_id, student_id);
