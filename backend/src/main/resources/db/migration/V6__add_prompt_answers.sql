CREATE TABLE IF NOT EXISTS prompt_answers (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    prompt_index INT NOT NULL,
    answer_text VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_user_prompt UNIQUE (user_id, prompt_index)
);
