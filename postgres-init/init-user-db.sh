#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
-- 1) 기존 테이블 삭제 (의존성 순서대로)
DROP TABLE IF EXISTS	histories				CASCADE;
DROP TABLE IF EXISTS	group_members			CASCADE;
DROP TABLE IF EXISTS	groups					CASCADE;
DROP TABLE IF EXISTS	course_likes			CASCADE;
DROP TABLE IF EXISTS	courses					CASCADE;
DROP TABLE IF EXISTS	users					CASCADE;
DROP TABLE IF EXISTS	curricula				CASCADE;
DROP TABLE IF EXISTS	marathons				CASCADE;
DROP TABLE IF EXISTS	marathon_distances		CASCADE;
DROP TABLE IF EXISTS	todos					CASCADE;

-- 2) UUID 생성용 확장 활성화
CREATE EXTENSION IF NOT EXISTS	pgcrypto;

-- 세션 타임존을 Asia/Seoul로 설정
SET TIME ZONE				'Asia/Seoul';

-- 3) 테이블 재생성

-- 사용자 테이블
CREATE TABLE	users (
	user_id			UUID				PRIMARY KEY DEFAULT gen_random_uuid(),
	email			VARCHAR(255)		UNIQUE NOT NULL,
	password		VARCHAR(100)		NOT NULL,
	nickname		VARCHAR(8)			UNIQUE NOT NULL,
	profile_image	VARCHAR(50),
	avg_pace		DOUBLE PRECISION,
	created_at		TIMESTAMPTZ			NOT NULL DEFAULT now()
);

-- 코스 테이블
CREATE TABLE	courses (
	course_id		UUID				PRIMARY KEY DEFAULT gen_random_uuid(),
	course_name		VARCHAR(15)			NOT NULL,
	is_shared		BOOLEAN				NOT NULL DEFAULT FALSE,
	distance		DOUBLE PRECISION	NOT NULL,
	avg_elevation	DOUBLE PRECISION	NOT NULL,
	start_location	VARCHAR(30),
	gpx_file		VARCHAR(50),
	created_by		UUID				NOT NULL REFERENCES users(user_id),
	created_at		TIMESTAMPTZ			NOT NULL DEFAULT now()
);

-- 코스 좋아요 테이블
CREATE TABLE	course_likes (
	like_id			UUID				PRIMARY KEY DEFAULT gen_random_uuid(),
	user_id			UUID				NOT NULL REFERENCES users(user_id),
	course_id		UUID				NOT NULL REFERENCES courses(course_id),
	liked_at		TIMESTAMPTZ			NOT NULL DEFAULT now()
);

-- 그룹 테이블
CREATE TABLE	groups (
	group_id		UUID				PRIMARY KEY DEFAULT gen_random_uuid(),
	group_name		VARCHAR(20)			NOT NULL,
	leader_id		UUID				NOT NULL REFERENCES users(user_id),
	course_id		UUID				REFERENCES courses(course_id),
	start_time		TIMESTAMPTZ			NOT NULL,
	start_location	VARCHAR(30),
	latitude		DOUBLE PRECISION,
	longitude		DOUBLE PRECISION,
	invite_code		VARCHAR(10)			NOT NULL,
	status          SMALLINT       NOT NULL DEFAULT 0,                  -- 상태: 0=시작 전, 1=진행 중, 2=완료
	CONSTRAINT chk_group_status CHECK (status IN (0, 1, 2))              -- 유효 값 제한
);

-- 그룹 멤버 테이블
CREATE TABLE	group_members (
	member_id		UUID				PRIMARY KEY DEFAULT gen_random_uuid(),
	group_id		UUID				NOT NULL REFERENCES groups(group_id),
	user_id			UUID				NOT NULL REFERENCES users(user_id),
	joined_at		TIMESTAMPTZ			NOT NULL DEFAULT now(),
	is_finished		BOOLEAN				NOT NULL DEFAULT FALSE
);

-- 달리기 기록 테이블
CREATE TABLE	histories (
	history_id		UUID				PRIMARY KEY DEFAULT gen_random_uuid(),
	user_id			UUID				NOT NULL REFERENCES users(user_id),
	course_id		UUID				REFERENCES courses(course_id),
	group_id		UUID				REFERENCES groups(group_id),
	gpx_file		VARCHAR(50)			NOT NULL,
	start_location	VARCHAR(30),
	start_time		TIMESTAMPTZ			NOT NULL,
	end_time		TIMESTAMPTZ			NOT NULL,
	distance		DOUBLE PRECISION	NOT NULL,
	avg_bpm			DOUBLE PRECISION,
	avg_pace		DOUBLE PRECISION,
	avg_cadence		DOUBLE PRECISION,
	avg_elevation	DOUBLE PRECISION,
	calories		DOUBLE PRECISION,
	created_at		TIMESTAMPTZ			NOT NULL DEFAULT now()
);

-- 마라톤 테이블
CREATE TABLE	marathons (
	marathon_id		UUID				PRIMARY KEY DEFAULT gen_random_uuid(),
	name			VARCHAR(30)			NOT NULL,
	date			TIMESTAMPTZ			NOT NULL,
	location		VARCHAR(30)			NOT NULL,
	created_at		TIMESTAMPTZ			NOT NULL DEFAULT now()
);

-- 마라톤 거리 테이블
CREATE TABLE	marathon_distances (
	distance_id		UUID				PRIMARY KEY DEFAULT gen_random_uuid(),
	marathon_id		UUID				NOT NULL REFERENCES marathons(marathon_id),
	distance		VARCHAR(30)			NOT NULL
);

-- 커리큘럼 테이블
CREATE TABLE	curricula (
	curriculum_id	UUID				PRIMARY KEY DEFAULT gen_random_uuid(),
	user_id			UUID				NOT NULL REFERENCES users(user_id),
	marathon_id		UUID				REFERENCES marathons(marathon_id),
	goal_dist		VARCHAR(10)			NOT NULL,
	goal_date		TIMESTAMPTZ			NOT NULL,
	run_exp			BOOLEAN,
	dist_exp		VARCHAR(10),
	freq_exp		VARCHAR(10)
);

-- 커리큘럼 ToDo 테이블
CREATE TABLE	todos (
	todo_id			UUID				PRIMARY KEY DEFAULT gen_random_uuid(),
	curriculum_id	UUID				NOT NULL REFERENCES curricula(curriculum_id),
	content			VARCHAR(500)		NOT NULL,
	is_done			BOOLEAN				NOT NULL DEFAULT FALSE,
	date			TIMESTAMPTZ			NOT NULL
);
EOSQL
