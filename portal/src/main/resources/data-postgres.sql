INSERT INTO roles (name)
VALUES ('ROLE_STUDENT'), ('ROLE_PROVIDER'), ('ROLE_ADMIN')
ON CONFLICT (name) DO NOTHING;

INSERT INTO subscription_plans (
    name, type, price, duration_days, max_applications, max_bookings, description, active
)
VALUES
    ('Free', 'FREE', 0.00, 365, 3, 2, 'Basic free access', true),
    ('Basic', 'BASIC', 9.99, 30, 10, 5, 'Perfect for students starting their journey', true),
    ('Premium', 'PREMIUM', 29.99, 30, 50, 20, 'Full access to all features', true),
    ('Pro', 'PRO', 99.99, 30, -1, -1, 'Unlimited access for power users', true)
ON CONFLICT (type) DO NOTHING;

INSERT INTO service_categories (name, description, icon, active)
VALUES
    ('Visa Assistance', 'Help with student visa applications', 'visa', true),
    ('Accommodation', 'Finding student housing and hostels', 'home', true),
    ('Airport Pickup', 'Transport from airport to accommodation', 'flight', true),
    ('Bank Account Setup', 'Opening a student bank account', 'bank', true),
    ('Health Insurance', 'Student health and travel insurance', 'health', true),
    ('Language Courses', 'Language preparation and tutoring', 'language', true),
    ('Career Counselling', 'Guidance for job placement abroad', 'career', true),
    ('Document Translation', 'Certified document translation', 'document', true)
ON CONFLICT (name) DO NOTHING;
