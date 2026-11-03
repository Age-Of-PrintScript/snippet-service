CREATE TABLE lint_rule (
    id INT PRIMARY KEY,
    lint_rule VARCHAR(100) NOT NULL UNIQUE,
    default_value VARCHAR(50)
);

INSERT INTO lint_rule (id, lint_rule, default_value) VALUES
    (1, 'identifier-format', 'camelCase'),
    (2, 'println-no-expression', NULL),
    (3, 'readInput-no-expression', NULL);

CREATE TABLE format_rule (
    id INT PRIMARY KEY,
    format_rule VARCHAR(100) NOT NULL UNIQUE,
    default_value INT
);

INSERT INTO format_rule (id, format_rule, default_value) VALUES
    (1, 'enforce-spacing-around-equals', NULL),
    (2, 'enforce-no-spacing-around-equals', NULL),
    (3, 'enforce-spacing-before-colon-in-declaration', NULL),
    (4, 'enforce-spacing-after-colon-in-declaration', NULL),
    (5, 'mandatory-single-space-separation', NULL),
    (6, 'mandatory-space-surrounding-operations', NULL),
    (7, 'if-brace-same-line', NULL),
    (8, 'if-brace-below-line', NULL),
    (9, 'indent-inside-if', 4),
    (10, 'line-breaks-after-println', 1);

CREATE TABLE active_lint_rule (
    user_id VARCHAR(128) NOT NULL,
    lint_rule_id INT NOT NULL REFERENCES lint_rule(id) ON DELETE CASCADE,
    value VARCHAR(50),
    PRIMARY KEY (user_id, lint_rule_id)
);

CREATE INDEX idx_active_lint_rule_user_id ON active_lint_rule(user_id);

CREATE TABLE active_format_rule (
    user_id VARCHAR(128) NOT NULL,
    format_rule_id INT NOT NULL REFERENCES format_rule(id) ON DELETE CASCADE,
    value INT,
    PRIMARY KEY (user_id, format_rule_id)
);

CREATE INDEX idx_active_format_rule_user_id ON active_format_rule(user_id);
