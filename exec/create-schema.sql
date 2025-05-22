-- 1) pgcrypto 익스텐션 (UUID 생성용)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 2) ENUM 타입 생성 (성별)
DO ' BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = ''gender_enum'') THEN
        CREATE TYPE gender_enum AS ENUM (''MALE'', ''FEMALE'');
    END IF;
END ';


-- 3) 기존 테이블 삭제 (의존성에 따라 역순으로 나열)
DROP TABLE IF EXISTS
  todos,
  marathon_distances,
  histories,
  group_members,
  groups,
  curricula,
  course_likes,
  courses,
  users,
  marathons
CASCADE;

-- 4) 테이블 생성

-- 4.1) 마라톤 정보
CREATE TABLE marathons (
  marathon_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name         VARCHAR(30)      NOT NULL,
  date         TIMESTAMPTZ       NOT NULL,
  location     VARCHAR(30)       NOT NULL,
  created_at   TIMESTAMPTZ       NOT NULL DEFAULT now()
);

-- 4.2) 사용자 정보
CREATE TABLE users (
  user_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email         VARCHAR(255)     NOT NULL UNIQUE,
  password      VARCHAR(100)     NOT NULL,
  nickname      VARCHAR(8)       NOT NULL,
  profile_image VARCHAR(50),
  avg_pace      FLOAT8,
  gender        gender_enum,
  birthday      DATE,
  fcm_token     VARCHAR(255),
  height        FLOAT8,
  weight        FLOAT8,
  created_at    TIMESTAMPTZ       NOT NULL DEFAULT now()
);

-- 4.3) 코스
CREATE TABLE courses (
  course_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  course_name   VARCHAR(15)      NOT NULL,
  is_shared     BOOLEAN          NOT NULL DEFAULT FALSE,
  distance      FLOAT8           NOT NULL,
  avg_elevation FLOAT8           NOT NULL,
  start_location VARCHAR(30),
  gpx_file      VARCHAR(50),
  created_by    UUID             NOT NULL REFERENCES users(user_id),
  created_at    TIMESTAMPTZ      NOT NULL DEFAULT now()
);

-- 4.4) 코스 좋아요
CREATE TABLE course_likes (
  like_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id       UUID             NOT NULL REFERENCES users(user_id),
  course_id     UUID             NOT NULL REFERENCES courses(course_id),
  liked_at      TIMESTAMPTZ      NOT NULL DEFAULT now()
);

-- 4.5) 커리큘럼
CREATE TABLE curricula (
  curriculum_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id       UUID             NOT NULL REFERENCES users(user_id),
  marathon_id   UUID             REFERENCES marathons(marathon_id),
  goal_dist     VARCHAR(32)      NOT NULL,
  goal_date     TIMESTAMPTZ      NOT NULL,
  run_exp       BOOLEAN,
  dist_exp      VARCHAR(32),
  freq_exp      VARCHAR(32),
  is_finished   BOOLEAN          NOT NULL DEFAULT FALSE
);

-- 4.6) 그룹
CREATE TABLE groups (
  group_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  group_name     VARCHAR(20)      NOT NULL,
  leader_id      UUID              NOT NULL REFERENCES users(user_id),
  course_id      UUID              REFERENCES courses(course_id),
  start_time     TIMESTAMPTZ       NOT NULL,
  start_location VARCHAR(30),
  latitude       FLOAT8,
  longitude      FLOAT8,
  invite_code    VARCHAR(10)       NOT NULL,
  status         SMALLINT          NOT NULL DEFAULT 0
    CHECK (status IN (0,1,2))
);

-- 4.7) 그룹 멤버
CREATE TABLE group_members (
  member_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  group_id      UUID              NOT NULL REFERENCES groups(group_id),
  user_id       UUID              NOT NULL REFERENCES users(user_id),
  joined_at     TIMESTAMPTZ       NOT NULL DEFAULT now(),
  is_finished   BOOLEAN           NOT NULL DEFAULT FALSE
);

-- 4.8) 기록(histories)
CREATE TABLE histories (
  history_id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id       UUID              NOT NULL REFERENCES users(user_id),
  course_id     UUID              REFERENCES courses(course_id),
  group_id      UUID              REFERENCES groups(group_id),
  gpx_file      VARCHAR(50)       NOT NULL,
  start_location VARCHAR(30),
  start_time    TIMESTAMPTZ       NOT NULL,
  end_time      TIMESTAMPTZ       NOT NULL,
  distance      FLOAT8            NOT NULL,
  avg_bpm       FLOAT8,
  avg_pace      FLOAT8,
  avg_cadence   FLOAT8,
  avg_elevation FLOAT8,
  calories      FLOAT8,
  created_at    TIMESTAMPTZ       NOT NULL DEFAULT now()
);

-- 4.9) 마라톤 거리 옵션
CREATE TABLE marathon_distances (
  distance_id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  marathon_id   UUID               NOT NULL REFERENCES marathons(marathon_id),
  distance      VARCHAR(30)        NOT NULL
);

-- 4.10) 할 일(todo)
CREATE TABLE todos (
  todo_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  curriculum_id  UUID               NOT NULL REFERENCES curricula(curriculum_id),
  user_id        UUID               NOT NULL REFERENCES users(user_id),
  content        VARCHAR(500)       NOT NULL,
  is_done        BOOLEAN,
  date           TIMESTAMPTZ        NOT NULL
);

-- 5) 외래키 컬럼 인덱스 추가 (성능 개선)
CREATE INDEX idx_courses_created_by       ON courses(created_by);
CREATE INDEX idx_course_likes_user_id     ON course_likes(user_id);
CREATE INDEX idx_course_likes_course_id   ON course_likes(course_id);
CREATE INDEX idx_curricula_user_id        ON curricula(user_id);
CREATE INDEX idx_curricula_marathon_id    ON curricula(marathon_id);
CREATE INDEX idx_groups_leader_id         ON groups(leader_id);
CREATE INDEX idx_groups_course_id         ON groups(course_id);
CREATE INDEX idx_group_members_group_id   ON group_members(group_id);
CREATE INDEX idx_group_members_user_id    ON group_members(user_id);
CREATE INDEX idx_histories_user_id        ON histories(user_id);
CREATE INDEX idx_histories_course_id      ON histories(course_id);
CREATE INDEX idx_histories_group_id       ON histories(group_id);
CREATE INDEX idx_marathon_distances_marathon_id
                                         ON marathon_distances(marathon_id);
CREATE INDEX idx_todos_curriculum_id       ON todos(curriculum_id);
CREATE INDEX idx_todos_user_id             ON todos(user_id);
