--
-- PostgreSQL database dump
--

-- Dumped from database version 15.12 (Debian 15.12-1.pgdg120+1)
-- Dumped by pg_dump version 17.4

-- Started on 2025-05-22 11:11:50

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

DROP DATABASE d107;
--
-- TOC entry 3504 (class 1262 OID 16384)
-- Name: d107; Type: DATABASE; Schema: -; Owner: runhwani
--

CREATE DATABASE d107 WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'en_US.utf8';


ALTER DATABASE d107 OWNER TO runhwani;

\connect d107

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 2 (class 3079 OID 24581)
-- Name: pgcrypto; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;


--
-- TOC entry 3505 (class 0 OID 0)
-- Dependencies: 2
-- Name: EXTENSION pgcrypto; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pgcrypto IS 'cryptographic functions';


--
-- TOC entry 883 (class 1247 OID 24753)
-- Name: gender_enum; Type: TYPE; Schema: public; Owner: runhwani
--

CREATE TYPE public.gender_enum AS ENUM (
    'MALE',
    'FEMALE'
);


ALTER TYPE public.gender_enum OWNER TO runhwani;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 218 (class 1259 OID 26018)
-- Name: course_likes; Type: TABLE; Schema: public; Owner: runhwani
--

CREATE TABLE public.course_likes (
    like_id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid NOT NULL,
    course_id uuid NOT NULL,
    liked_at timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.course_likes OWNER TO runhwani;

--
-- TOC entry 217 (class 1259 OID 26005)
-- Name: courses; Type: TABLE; Schema: public; Owner: runhwani
--

CREATE TABLE public.courses (
    course_id uuid DEFAULT gen_random_uuid() NOT NULL,
    course_name character varying(15) NOT NULL,
    is_shared boolean DEFAULT false NOT NULL,
    distance double precision NOT NULL,
    avg_elevation double precision NOT NULL,
    start_location character varying(30),
    gpx_file character varying(50),
    created_by uuid NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.courses OWNER TO runhwani;

--
-- TOC entry 219 (class 1259 OID 26035)
-- Name: curricula; Type: TABLE; Schema: public; Owner: runhwani
--

CREATE TABLE public.curricula (
    curriculum_id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid NOT NULL,
    marathon_id uuid,
    goal_dist character varying(32) NOT NULL,
    goal_date timestamp with time zone NOT NULL,
    run_exp boolean,
    dist_exp character varying(32),
    freq_exp character varying(32),
    is_finished boolean DEFAULT false NOT NULL
);


ALTER TABLE public.curricula OWNER TO runhwani;

--
-- TOC entry 221 (class 1259 OID 26070)
-- Name: group_members; Type: TABLE; Schema: public; Owner: runhwani
--

CREATE TABLE public.group_members (
    member_id uuid DEFAULT gen_random_uuid() NOT NULL,
    group_id uuid NOT NULL,
    user_id uuid NOT NULL,
    joined_at timestamp with time zone DEFAULT now() NOT NULL,
    is_finished boolean DEFAULT false NOT NULL
);


ALTER TABLE public.group_members OWNER TO runhwani;

--
-- TOC entry 220 (class 1259 OID 26052)
-- Name: groups; Type: TABLE; Schema: public; Owner: runhwani
--

CREATE TABLE public.groups (
    group_id uuid DEFAULT gen_random_uuid() NOT NULL,
    group_name character varying(20) NOT NULL,
    leader_id uuid NOT NULL,
    course_id uuid,
    start_time timestamp with time zone NOT NULL,
    start_location character varying(30),
    latitude double precision,
    longitude double precision,
    invite_code character varying(10) NOT NULL,
    status smallint DEFAULT 0 NOT NULL,
    CONSTRAINT groups_status_check CHECK ((status = ANY (ARRAY[0, 1, 2])))
);


ALTER TABLE public.groups OWNER TO runhwani;

--
-- TOC entry 222 (class 1259 OID 26088)
-- Name: histories; Type: TABLE; Schema: public; Owner: runhwani
--

CREATE TABLE public.histories (
    history_id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid NOT NULL,
    course_id uuid,
    group_id uuid,
    gpx_file character varying(50) NOT NULL,
    start_location character varying(30),
    start_time timestamp with time zone NOT NULL,
    end_time timestamp with time zone NOT NULL,
    distance double precision NOT NULL,
    avg_bpm double precision,
    avg_pace double precision,
    avg_cadence double precision,
    avg_elevation double precision,
    calories double precision,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.histories OWNER TO runhwani;

--
-- TOC entry 223 (class 1259 OID 26110)
-- Name: marathon_distances; Type: TABLE; Schema: public; Owner: runhwani
--

CREATE TABLE public.marathon_distances (
    distance_id uuid DEFAULT gen_random_uuid() NOT NULL,
    marathon_id uuid NOT NULL,
    distance character varying(30) NOT NULL
);


ALTER TABLE public.marathon_distances OWNER TO runhwani;

--
-- TOC entry 215 (class 1259 OID 25987)
-- Name: marathons; Type: TABLE; Schema: public; Owner: runhwani
--

CREATE TABLE public.marathons (
    marathon_id uuid DEFAULT gen_random_uuid() NOT NULL,
    name character varying(30) NOT NULL,
    date timestamp with time zone NOT NULL,
    location character varying(30) NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.marathons OWNER TO runhwani;

--
-- TOC entry 224 (class 1259 OID 26121)
-- Name: todos; Type: TABLE; Schema: public; Owner: runhwani
--

CREATE TABLE public.todos (
    todo_id uuid DEFAULT gen_random_uuid() NOT NULL,
    curriculum_id uuid NOT NULL,
    user_id uuid NOT NULL,
    content character varying(500) NOT NULL,
    is_done boolean,
    date timestamp with time zone NOT NULL
);


ALTER TABLE public.todos OWNER TO runhwani;

--
-- TOC entry 216 (class 1259 OID 25994)
-- Name: users; Type: TABLE; Schema: public; Owner: runhwani
--

CREATE TABLE public.users (
    user_id uuid DEFAULT gen_random_uuid() NOT NULL,
    email character varying(255) NOT NULL,
    password character varying(100) NOT NULL,
    nickname character varying(8) NOT NULL,
    profile_image character varying(50),
    avg_pace double precision,
    gender public.gender_enum,
    birthday date,
    fcm_token character varying(255),
    height double precision,
    weight double precision,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.users OWNER TO runhwani;

--
-- TOC entry 3492 (class 0 OID 26018)
-- Dependencies: 218
-- Data for Name: course_likes; Type: TABLE DATA; Schema: public; Owner: runhwani
--

COPY public.course_likes (like_id, user_id, course_id, liked_at) FROM stdin;
\.


--
-- TOC entry 3491 (class 0 OID 26005)
-- Dependencies: 217
-- Data for Name: courses; Type: TABLE DATA; Schema: public; Owner: runhwani
--

COPY public.courses (course_id, course_name, is_shared, distance, avg_elevation, start_location, gpx_file, created_by, created_at) FROM stdin;
0a85daa1-8b42-457f-81be-dabf9141e402	선혁아놀자	t	0.051959947	11.783332824707031	경북 구미시 임수동 94-1	https://k12d107.p.ssafy.io/gpx/eeadf92b.gpx	a886fafd-b8c3-4c81-91af-dc3df66c71bd	2025-05-21 04:51:31.309379+00
423fdba7-24ea-41be-8bde-d7ffa61dae00	a	t	0.014549317	35.349998474121094	경북 구미시 임수동 94-1	https://k12d107.p.ssafy.io/gpx/7a5e6285.gpx	a243b676-df47-42c3-a6cb-b91d60410ffd	2025-05-21 05:33:37.416382+00
8efc7000-ae53-45eb-9682-61fcfb549050	qqq	t	0.00403983	141.39999389648438	경북 구미시 임수동 94-1	https://k12d107.p.ssafy.io/gpx/449efe0a.gpx	063cf97b-0c40-4c82-a4f7-4f711ea11578	2025-05-21 14:11:09.96844+00
8333435b-06d1-4706-bea9-1ac2ecd0aceb	ㅎㅇ	f	0.005218314	141.5999984741211	경북 구미시 임수동 94-1	https://k12d107.p.ssafy.io/gpx/ff4ba6f1.gpx	a243b676-df47-42c3-a6cb-b91d60410ffd	2025-05-21 14:31:27.205792+00
6791a321-f40b-40d0-a2a8-daee2ac810a9	코쓱	t	0.0029100138	70.79999923706055	경북 구미시 임수동 94-1	https://k12d107.p.ssafy.io/gpx/d7c9053d.gpx	a886fafd-b8c3-4c81-91af-dc3df66c71bd	2025-05-21 14:32:17.019066+00
8cea1f49-4539-4679-9ed2-6afc76236c30	소금빵코스	t	0.4093575	92.6470588235294	경북 구미시 황상동 304-4	https://k12d107.p.ssafy.io/gpx/f719e162.gpx	a886fafd-b8c3-4c81-91af-dc3df66c71bd	2025-05-21 16:43:19.920005+00
272e47c5-76ad-4113-8f9d-555156adfb5e	싸피한바퀴	t	0.8751321	97.5126050420168	경북 구미시 황상동 304-10	https://k12d107.p.ssafy.io/gpx/d6dc5032.gpx	a886fafd-b8c3-4c81-91af-dc3df66c71bd	2025-05-21 16:45:37.818057+00
935350bc-261d-4cf2-a39b-a5a49ddaf9a8	싸피	f	0.04124416	47.133331298828125	경북 구미시 임수동 94-1	https://k12d107.p.ssafy.io/gpx/f583cb9e.gpx	6d084e9d-d896-45d1-af00-1f5063415366	2025-05-22 02:08:46.190279+00
1faf3563-7583-4383-b451-f1a945aa3501	싸피2	t	0.00026079232	141.39999389648438	경북 구미시 임수동 94-1	https://k12d107.p.ssafy.io/gpx/aecdcd8c.gpx	6d084e9d-d896-45d1-af00-1f5063415366	2025-05-22 02:09:21.453313+00
0208d32b-650e-469e-a120-a40b0023db65	싸피2	f	0.00026079232	141.39999389648438	경북 구미시 임수동 94-1	https://k12d107.p.ssafy.io/gpx/aecdcd8c.gpx	6d084e9d-d896-45d1-af00-1f5063415366	2025-05-22 02:09:34.002485+00
\.


--
-- TOC entry 3493 (class 0 OID 26035)
-- Dependencies: 219
-- Data for Name: curricula; Type: TABLE DATA; Schema: public; Owner: runhwani
--

COPY public.curricula (curriculum_id, user_id, marathon_id, goal_dist, goal_date, run_exp, dist_exp, freq_exp, is_finished) FROM stdin;
16e0bf2b-476b-4278-9a87-8dc8f6237bf2	a243b676-df47-42c3-a6cb-b91d60410ffd	\N	10km	2025-06-21 04:17:07.602692+00	f	~10km	3~4회	t
5b443817-793b-4ae9-8371-a5f0eded7cbc	a243b676-df47-42c3-a6cb-b91d60410ffd	\N	5km	2025-05-22 15:00:00+00	f	하프	1~2회	t
38810c5d-ccbe-43fa-851b-a795f3211803	a243b676-df47-42c3-a6cb-b91d60410ffd	\N	10km	2025-05-26 15:00:00+00	f	하프	1~2회	t
b28ce7cc-2b08-443a-83ed-aa15c4a6bdde	a243b676-df47-42c3-a6cb-b91d60410ffd	\N	하프(21km)	2025-05-27 15:00:00+00	f	하프	1~2회	t
98fdce08-75cd-4837-a420-bc51bcc90e0e	a243b676-df47-42c3-a6cb-b91d60410ffd	ef1a1671-858d-4586-b92f-67898efe2cca	11.8km	2025-06-20 15:00:00+00	f	하프	1~2회	f
efc67d2c-8cf1-4f7a-b45f-51e9326d87b9	063cf97b-0c40-4c82-a4f7-4f711ea11578	\N	20km	2025-06-10 00:00:00+00	f	~5km	1~2회	t
de6f8ae8-a899-48c2-bf93-18721e08f085	063cf97b-0c40-4c82-a4f7-4f711ea11578	\N	20km	2025-06-30 00:00:00+00	f	~5km	1~2회	t
8d9300af-2eb3-4430-b9b2-18926e6a7e9a	063cf97b-0c40-4c82-a4f7-4f711ea11578	\N	20km	2025-06-30 00:00:00+00	f	~5km	1~2회	t
bb784f0d-35e0-4bb3-bb22-1be60d993dad	063cf97b-0c40-4c82-a4f7-4f711ea11578	\N	20km	2025-06-30 00:00:00+00	f	~5km	1~2회	t
618577fc-4f5f-43e9-b63e-bfd9d8b083bb	063cf97b-0c40-4c82-a4f7-4f711ea11578	\N	20km	2025-06-30 00:00:00+00	f	~5km	1~2회	t
eef71642-b88f-41ad-815f-05cb544d3b65	063cf97b-0c40-4c82-a4f7-4f711ea11578	\N	20km	2025-06-30 00:00:00+00	f	~5km	1~2회	t
803c5eb2-3caa-4b91-b2ed-ceb869832149	063cf97b-0c40-4c82-a4f7-4f711ea11578	\N	20km	2025-06-30 00:00:00+00	f	~5km	1~2회	t
243e7e77-5922-4aa0-b4da-d26e906b5a67	063cf97b-0c40-4c82-a4f7-4f711ea11578	\N	10km	2025-06-20 00:00:00+00	f	~5km	1~2회	t
2d51d0c3-d327-4bd3-8597-cb25dd4ac24d	063cf97b-0c40-4c82-a4f7-4f711ea11578	\N	20km	2025-06-20 00:00:00+00	f	~5km	1~2회	t
4ae429ee-ff90-4e21-8b81-586e94ff3a33	962c3730-2d81-43fb-a4ac-30e2196a81c3	9b8f40d4-7e04-4521-a3cb-32070a2a7a55	10km	2025-05-23 15:00:00+00	t	~10km	1~2회	f
6687b3a2-2eca-4b88-a1d5-68325a5c56be	063cf97b-0c40-4c82-a4f7-4f711ea11578	194de71f-fb52-4f26-b90c-ffd3e3bd2851	10km	2025-05-24 15:00:00+00	t	하프	3~4회	t
3e7f07c2-a59d-415d-b661-3030bfd71d72	063cf97b-0c40-4c82-a4f7-4f711ea11578	b1886f12-9e4f-4e59-97e7-30272acf014b	하프(21.0975km)	2025-06-14 15:00:00+00	t	풀	5회 이상	t
be0fbf27-75ac-4e36-b24b-c5f550aa4c43	063cf97b-0c40-4c82-a4f7-4f711ea11578	\N	풀(42km)	2025-06-21 15:00:00+00	t	풀	5회 이상	t
4e3c1959-b113-455c-98ff-2edcfb1b0250	063cf97b-0c40-4c82-a4f7-4f711ea11578	\N	10km	2025-06-15 15:00:00+00	f	10km	1~2회	t
588a0806-167a-47f4-b49e-9c6e0b968929	063cf97b-0c40-4c82-a4f7-4f711ea11578	\N	20km	2025-06-20 00:00:00+00	f	~5km	1~2회	t
812960c6-1710-4d82-b607-9dc88c91f2fc	063cf97b-0c40-4c82-a4f7-4f711ea11578	\N	20km	2025-06-20 00:00:00+00	f	~5km	1~2회	t
517fd83a-e1ce-4858-9438-007628b575a5	063cf97b-0c40-4c82-a4f7-4f711ea11578	\N	20km	2025-06-20 00:00:00+00	f	~5km	1~2회	t
464f7773-c715-4e45-a662-24067ec6c359	063cf97b-0c40-4c82-a4f7-4f711ea11578	\N	20km	2025-06-20 00:00:00+00	f	~5km	1~2회	t
d9577eae-368e-4769-8bc4-520e858f3200	063cf97b-0c40-4c82-a4f7-4f711ea11578	\N	20km	2025-06-20 00:00:00+00	f	~5km	1~2회	t
5bf9876b-20aa-474f-8e7c-d7d33e3e7856	063cf97b-0c40-4c82-a4f7-4f711ea11578	\N	20km	2025-06-20 00:00:00+00	f	~5km	1~2회	t
7b49e9a9-1341-467d-90a2-4ed69e0e5eca	063cf97b-0c40-4c82-a4f7-4f711ea11578	\N	20km	2025-06-20 00:00:00+00	f	~5km	1~2회	t
2ec8e248-4fb3-475a-9573-fb5c52551222	063cf97b-0c40-4c82-a4f7-4f711ea11578	\N	20km	2025-06-20 00:00:00+00	f	~5km	1~2회	t
72f6038a-7bc5-4b8e-865f-754f191bdd8e	063cf97b-0c40-4c82-a4f7-4f711ea11578	b1886f12-9e4f-4e59-97e7-30272acf014b	10km	2025-06-14 15:00:00+00	f	5km	0회	f
7576330d-d2f0-420e-a999-5bdd254b79df	417b3d0e-c29e-4840-a043-415f15c45b6b	92cb209a-7ac2-427b-bf55-ea64da9e9ad7	10km	2025-06-06 15:00:00+00	f	5km	0회	f
5c7d7e3b-9115-4a5a-aa04-80742fbbc693	1a27f815-0c6c-4bf1-b2c9-9cd88dd9ea44	26daa80e-af0e-4e35-9c37-779e77ae5e63	10km	2025-06-06 15:00:00+00	t	~10km	3~4회	f
00d3da65-f7d0-4285-978a-42ce538d1739	dfeff5ca-d525-4062-892a-f56b3844f724	c12b5959-e634-49ef-b23c-4a7bcfda0b13	15km	2025-06-14 15:00:00+00	t	~10km	3~4회	f
b14cf416-cdb0-4f4f-a0cf-d593d2d4881a	0f9c3869-310c-48c5-b838-c50fa0ad5bc1	34b07196-0420-4c90-b78f-761daa4703c1	12km	2025-06-13 15:00:00+00	t	~10km	1~2회	t
34ea3a89-2625-4148-9879-6e39d41cc181	6ffb74df-9bc4-41ed-b2b8-2b949e07f0b2	eff919b0-5ca6-4355-be21-466330ec7683	10km	2025-05-30 15:00:00+00	f	~10km	0회	f
11480fa0-d596-4f74-8b42-3120e3869484	0f9c3869-310c-48c5-b838-c50fa0ad5bc1	1e46bd10-2f9a-423a-946e-06d61ab1820c	5km	2025-06-21 15:00:00+00	t	하프	1~2회	t
21767762-edec-4078-9f7b-21f26dee68d4	0f9c3869-310c-48c5-b838-c50fa0ad5bc1	1a6b7b21-d5f8-4d6b-8e67-971661eaa3c4	25km	2025-05-23 15:00:00+00	f	하프	1~2회	f
ca1ae130-c20e-48e7-b6d7-7fe8350f2b98	15b88134-e3c4-4822-9bfa-244c6a185913	1e46bd10-2f9a-423a-946e-06d61ab1820c	1031km	2025-06-21 15:00:00+00	t	하프	1~2회	f
2ff5653d-7a86-499f-9ffd-943b1b1b4a78	93db6be8-c5bb-4bf5-ac84-11fcf9dae304	92cb209a-7ac2-427b-bf55-ea64da9e9ad7	5km	2025-06-06 15:00:00+00	f	5km	0회	f
8c02026c-8c05-4302-9de7-b4b84b8a8907	6d084e9d-d896-45d1-af00-1f5063415366	a5da1a1d-0e81-49a8-9265-9b33877b202a	10km	2025-05-31 15:00:00+00	f	~10km	1~2회	f
\.


--
-- TOC entry 3495 (class 0 OID 26070)
-- Dependencies: 221
-- Data for Name: group_members; Type: TABLE DATA; Schema: public; Owner: runhwani
--

COPY public.group_members (member_id, group_id, user_id, joined_at, is_finished) FROM stdin;
41769274-4884-4b2f-b069-45a1bf578eea	5b6fa10c-3d64-4498-afa9-9a61758ac47a	1206a60d-2489-42c3-b67d-5dfc3948acfe	2025-05-22 00:36:06.4043+00	f
1c92d0d9-f42e-43bb-83b7-8dfee1ee2e16	46b77028-0356-4193-a662-86c00f2f2658	a886fafd-b8c3-4c81-91af-dc3df66c71bd	2025-05-21 04:46:52.657213+00	t
c0be30e1-9df9-45dd-b3a3-62f4866a850a	66b8e281-9f32-438f-bec3-609a057cb653	a886fafd-b8c3-4c81-91af-dc3df66c71bd	2025-05-21 04:49:38.477437+00	t
9b62070b-3773-4dce-87fe-91e5f3dd1e4f	8945247e-deb5-4ee3-83ff-face388a645e	a886fafd-b8c3-4c81-91af-dc3df66c71bd	2025-05-21 04:52:00.9784+00	t
dd2506ae-7338-408a-8bfc-d0841747d3cc	3bb51c89-4181-49e3-9321-4c5443c3d079	a886fafd-b8c3-4c81-91af-dc3df66c71bd	2025-05-21 04:55:34.846694+00	t
0bf733e2-a7a3-4ab0-89c9-49eff162c270	23248a1c-ff7d-4e04-9e54-f72392bc5da6	bb87e298-e9a8-43e1-a528-75082d365036	2025-05-21 12:48:08.169815+00	f
7e8c5050-754c-49bd-b25f-b8dbd7385fd4	23248a1c-ff7d-4e04-9e54-f72392bc5da6	a886fafd-b8c3-4c81-91af-dc3df66c71bd	2025-05-21 12:46:41.453067+00	t
7d1ca573-9a3b-4b5d-be3a-ce1a454d0047	e7cdafc3-8d99-4911-a17d-ae47dffba80c	bb87e298-e9a8-43e1-a528-75082d365036	2025-05-21 13:07:16.293541+00	f
e980b1b4-02fb-4c30-aed5-95f7e07fc028	e7cdafc3-8d99-4911-a17d-ae47dffba80c	a886fafd-b8c3-4c81-91af-dc3df66c71bd	2025-05-21 13:06:53.876073+00	t
6b37c8c7-160a-48e9-a6b5-5cba39b42459	1d8adb19-cbd3-40de-b305-6c9c1bea6fc3	1206a60d-2489-42c3-b67d-5dfc3948acfe	2025-05-21 13:25:18.51736+00	f
2ef6fbb7-cb69-489d-95ed-514df1cd3590	1d8adb19-cbd3-40de-b305-6c9c1bea6fc3	a886fafd-b8c3-4c81-91af-dc3df66c71bd	2025-05-21 13:24:52.755611+00	t
e8abf402-1011-4dea-be90-e604fb8c9bd5	abaaea07-9eb4-45d1-9eac-ba349b5e3ed5	417b3d0e-c29e-4840-a043-415f15c45b6b	2025-05-21 13:37:17.925319+00	t
76abf477-3c2a-4498-85ec-dfce48b6f052	bd43d1e8-6e69-43cb-9029-97fae5d7f081	417b3d0e-c29e-4840-a043-415f15c45b6b	2025-05-21 13:43:40.383309+00	t
cf768955-d132-4598-b84e-d472a3a80501	defaebbe-4c85-4ff3-a5be-71d89c1c4733	417b3d0e-c29e-4840-a043-415f15c45b6b	2025-05-21 15:46:50.333831+00	t
f08f4e60-29ca-4f4a-b91c-d4bad4df340b	c98493af-00a7-4e95-934d-474c61e8944d	0f9c3869-310c-48c5-b838-c50fa0ad5bc1	2025-05-21 16:17:09.766994+00	t
b686e9c0-378c-4990-95c4-977c0826e58e	b0bbb42c-6588-47ef-a9ad-1117871d6f00	0f9c3869-310c-48c5-b838-c50fa0ad5bc1	2025-05-21 16:47:42.5345+00	t
96c5d9e3-3825-4e7f-bb7f-32f2bac3f740	0aa89867-6592-4cf1-bca7-e567798cc2c5	0f9c3869-310c-48c5-b838-c50fa0ad5bc1	2025-05-21 16:55:46.432143+00	t
6dd79afa-a676-453c-8378-622525e7c5cc	29812557-16d3-48c8-8245-19b2b19daa75	6ffb74df-9bc4-41ed-b2b8-2b949e07f0b2	2025-05-21 15:48:30.641952+00	t
19ef10a7-0376-42a1-82c0-7980dfc42cee	f8b1748a-22e6-410e-bd68-993eed5b6625	6ffb74df-9bc4-41ed-b2b8-2b949e07f0b2	2025-05-22 00:23:18.428448+00	t
\.


--
-- TOC entry 3494 (class 0 OID 26052)
-- Dependencies: 220
-- Data for Name: groups; Type: TABLE DATA; Schema: public; Owner: runhwani
--

COPY public.groups (group_id, group_name, leader_id, course_id, start_time, start_location, latitude, longitude, invite_code, status) FROM stdin;
e7cdafc3-8d99-4911-a17d-ae47dffba80c	마이구미	a886fafd-b8c3-4c81-91af-dc3df66c71bd	\N	2025-05-21 13:34:00+00	경북 구미시 황상동 304-10	128.41778761521073	36.10844464778932	ba0eed46	2
5be1a0bd-e769-44af-bf86-b2feea060669	ㄱ	a243b676-df47-42c3-a6cb-b91d60410ffd	\N	2025-05-30 07:20:00+00	경북 구미시 황상동 502-5	128.41889483458064	36.11197414180789	9cc59f41	2
46b77028-0356-4193-a662-86c00f2f2658	찬우야놀자	a886fafd-b8c3-4c81-91af-dc3df66c71bd	\N	2025-05-21 04:48:00+00	경북 구미시 임수동 94-1	128.41657909999998	36.107103300000006	23cb9989	2
66b8e281-9f32-438f-bec3-609a057cb653	선혁아 들어와라	a886fafd-b8c3-4c81-91af-dc3df66c71bd	\N	2025-05-21 04:51:00+00	경북 구미시 임수동 94-1	128.4166115	36.1070989	b5ac82de	2
1d8adb19-cbd3-40de-b305-6c9c1bea6fc3	그룹그룹	a886fafd-b8c3-4c81-91af-dc3df66c71bd	\N	2025-05-21 13:31:00+00	경북 구미시 임수동 98	128.41771181937943	36.10850070812584	dc3b7e49	2
8945247e-deb5-4ee3-83ff-face388a645e	a	a243b676-df47-42c3-a6cb-b91d60410ffd	\N	2025-05-30 08:25:00+00	경기 성남시 분당구 삼평동 681	128.42388241970664	36.12045188739503	93f60293	2
3bb51c89-4181-49e3-9321-4c5443c3d079	연습그룹	a886fafd-b8c3-4c81-91af-dc3df66c71bd	\N	2025-05-21 05:55:00+00	경북 구미시 임수동 98	128.41736463260477	36.10702872235852	04a533c1	2
abaaea07-9eb4-45d1-9eac-ba349b5e3ed5	pppp	417b3d0e-c29e-4840-a043-415f15c45b6b	\N	2025-05-22 13:37:00+00	경북 구미시 임수동 94-1	128.4164989	36.1071296	cc9b8630	2
b9fc057f-8c3c-43e6-9adf-59a5a5f6b949	x	978a211a-d660-404b-b89e-bd115fc48532	\N	2025-05-23 07:25:00+00	경북 구미시 황상동 산 63	128.42177089932636	36.11887094092741	854fda78	2
a0212606-44b6-4ac2-9336-8b43255cacf8	a	a243b676-df47-42c3-a6cb-b91d60410ffd	\N	2025-05-30 07:20:00+00	경북 구미시 황상동 502-5	128.41863564080515	36.11150738046523	20f5cb4c	2
594c4d5f-9963-4971-ba74-4091489b041a	a	a243b676-df47-42c3-a6cb-b91d60410ffd	423fdba7-24ea-41be-8bde-d7ffa61dae00	2025-05-23 08:24:00+00	경북 구미시 임수동 94-1	128.4166233	36.1070737	9c8fee0d	2
0aa89867-6592-4cf1-bca7-e567798cc2c5	a	0f9c3869-310c-48c5-b838-c50fa0ad5bc1	\N	2025-05-22 20:55:00+00	경기 성남시 분당구 삼평동 681	128.42075848332473	36.11059514630779	e384eafa	2
bb58b720-a7f8-4ca4-8af9-7303b88c5019	a	a243b676-df47-42c3-a6cb-b91d60410ffd	423fdba7-24ea-41be-8bde-d7ffa61dae00	2025-05-24 07:25:00+00	경북 구미시 황상동 322-30	128.41806846825926	36.10970533187966	c6dd2d61	2
bd43d1e8-6e69-43cb-9029-97fae5d7f081	ooo	417b3d0e-c29e-4840-a043-415f15c45b6b	\N	2025-05-22 13:43:00+00	경기 성남시 분당구 삼평동 681	128.4165222	36.107113000000005	0b832c5f	2
78195365-8d2c-4e9b-a08a-71da62367f42	a	a243b676-df47-42c3-a6cb-b91d60410ffd	423fdba7-24ea-41be-8bde-d7ffa61dae00	2025-05-23 07:28:00+00	경북 구미시 임수동 94-1	128.4166233	36.1070737	a0d74dda	2
83c9bedc-b171-4bc6-8455-458e0555d674	h	a243b676-df47-42c3-a6cb-b91d60410ffd	423fdba7-24ea-41be-8bde-d7ffa61dae00	2025-05-30 07:20:00+00	경북 구미시 임수동 94-1	128.4166233	36.1070737	c62023eb	2
c7fcfb41-c04e-4c4c-bf95-d7a4d5e35e9e	a	a243b676-df47-42c3-a6cb-b91d60410ffd	\N	2025-05-30 08:25:00+00	경북 구미시 임수동 94-1	128.42574156547963	36.12511796639729	30c1f46c	2
312bb6e5-ed12-4db1-874e-4ffa119f9dfa	테스트1	063cf97b-0c40-4c82-a4f7-4f711ea11578	\N	2025-05-21 15:02:00+00	경북 구미시 임수동 94-1	128.4166116	36.1069343	328f5c82	2
78057c76-3a3c-4730-b6a6-b1a64d9bb935	a	a243b676-df47-42c3-a6cb-b91d60410ffd	\N	2025-05-30 08:34:00+00	경북 구미시 황상동 303-57	128.4184257587987	36.10897307715003	00fc358e	2
1b251047-5ca0-4526-b0ac-8b1f5dbbdc80	a	a243b676-df47-42c3-a6cb-b91d60410ffd	\N	2025-05-23 07:25:00+00	경북 구미시 황상동 422-2	128.42092128992346	36.112994370967634	25b0ab19	2
29812557-16d3-48c8-8245-19b2b19daa75	qwer	6ffb74df-9bc4-41ed-b2b8-2b949e07f0b2	\N	2025-05-21 17:48:00+00	경북 구미시 임수동 94-1	128.41655349999996	36.107108899999986	dc20de39	2
23248a1c-ff7d-4e04-9e54-f72392bc5da6	코쓱	a886fafd-b8c3-4c81-91af-dc3df66c71bd	\N	2025-05-21 13:00:00+00	경북 구미시 황상동 304-10	128.41774773451547	36.10832517945011	273710ed	2
defaebbe-4c85-4ff3-a5be-71d89c1c4733	ㅂ	417b3d0e-c29e-4840-a043-415f15c45b6b	6791a321-f40b-40d0-a2a8-daee2ac810a9	2025-05-22 19:28:00+00	경북 구미시 임수동 94-1	128.4165687	36.1070895	e1e8e872	2
51796884-b42a-443b-87b3-027e8d01cb05	ㅁ	063cf97b-0c40-4c82-a4f7-4f711ea11578	8efc7000-ae53-45eb-9682-61fcfb549050	2025-05-21 19:18:00+00	경북 구미시 임수동 94-1	128.4164904	36.1071081	6afa59bf	2
f8b1748a-22e6-410e-bd68-993eed5b6625	ㅈㅈ	6ffb74df-9bc4-41ed-b2b8-2b949e07f0b2	\N	2025-05-22 00:32:00+00	경북 구미시 임수동 94-1	128.4165833	36.107106099999996	8a6a9a7a	2
c98493af-00a7-4e95-934d-474c61e8944d	a	0f9c3869-310c-48c5-b838-c50fa0ad5bc1	\N	2025-05-30 19:20:00+00	경북 구미시 황상동 산 67	128.4229986920808	36.11740978740713	aa11cd30	2
b0bbb42c-6588-47ef-a9ad-1117871d6f00	a	0f9c3869-310c-48c5-b838-c50fa0ad5bc1	\N	2025-05-22 20:25:00+00	경북 구미시 황상동 305-2	128.4185907055695	36.1086864480658	a5b61440	2
5b6fa10c-3d64-4498-afa9-9a61758ac47a	달려라화니	063cf97b-0c40-4c82-a4f7-4f711ea11578	8cea1f49-4539-4679-9ed2-6afc76236c30	2025-05-22 01:10:00+00	경북 구미시 황상동 304-4	128.4179688	36.1084062	dab2ee6b	2
\.


--
-- TOC entry 3496 (class 0 OID 26088)
-- Dependencies: 222
-- Data for Name: histories; Type: TABLE DATA; Schema: public; Owner: runhwani
--

COPY public.histories (history_id, user_id, course_id, group_id, gpx_file, start_location, start_time, end_time, distance, avg_bpm, avg_pace, avg_cadence, avg_elevation, calories, created_at) FROM stdin;
68481955-3d58-4938-90cd-3788724b7bf9	a886fafd-b8c3-4c81-91af-dc3df66c71bd	\N	\N	https://k12d107.p.ssafy.io/gpx/360f569b.gpx	경북 구미시 임수동 94-1	2025-05-21 04:19:11+00	2025-05-21 04:19:53+00	0.022678449749946594	0	30.866370371639405	31.5	17.674999237060547	0	2025-05-21 04:19:56.159164+00
86ca4185-31b3-42a5-a742-9752d37548f8	a243b676-df47-42c3-a6cb-b91d60410ffd	\N	5be1a0bd-e769-44af-bf86-b2feea060669	https://k12d107.p.ssafy.io/gpx/504227c2.gpx	경북 구미시 임수동 94-1	2025-05-21 04:25:02+00	2025-05-21 04:26:13+00	0.024604110047221184	0	48.09503840259308	0	10.099999564034599	9.45	2025-05-21 04:26:16.781824+00
6222a449-2400-46a2-9adb-21cfc342cc62	a886fafd-b8c3-4c81-91af-dc3df66c71bd	\N	\N	https://k12d107.p.ssafy.io/gpx/3b373b90.gpx	경북 구미시 임수동 94-1	2025-05-21 04:39:27+00	2025-05-21 04:39:37+00	0.004319879226386547	0	46.29768634067476	0	73.12773011282484	0	2025-05-21 04:39:40.009322+00
24e2486c-13d3-4ad3-bebe-c50cbe6fc28d	a886fafd-b8c3-4c81-91af-dc3df66c71bd	\N	\N	https://k12d107.p.ssafy.io/gpx/80ba25e8.gpx	경북 구미시 임수동 94-1	2025-05-21 04:40:04+00	2025-05-21 04:40:19+00	0.0027476525865495205	0	109.18433095351216	64	49.337774127245204	0	2025-05-21 04:40:25.26608+00
35999d48-b169-42d6-ab73-753e8573a866	a886fafd-b8c3-4c81-91af-dc3df66c71bd	\N	46b77028-0356-4193-a662-86c00f2f2658	https://k12d107.p.ssafy.io/gpx/78fd6589.gpx	경북 구미시 임수동 94-1	2025-05-21 04:48:13+00	2025-05-21 04:49:00+00	0.014361368492245674	0	55.70511040528226	48	15.711110432942709	0	2025-05-21 04:49:06.391866+00
e41f8ccd-b1e8-4853-a492-2515bdda4ffb	a243b676-df47-42c3-a6cb-b91d60410ffd	\N	46b77028-0356-4193-a662-86c00f2f2658	https://k12d107.p.ssafy.io/gpx/4e551688.gpx	경북 구미시 임수동 94-1	2025-05-21 04:48:35+00	2025-05-21 04:49:08+00	0.006509720813483	0	84.4891986012882	0	28.279998779296875	0	2025-05-21 04:49:11.493408+00
405b764a-ece4-4c5e-8508-b6e3e7fbdbd0	a886fafd-b8c3-4c81-91af-dc3df66c71bd	0a85daa1-8b42-457f-81be-dabf9141e402	66b8e281-9f32-438f-bec3-609a057cb653	https://k12d107.p.ssafy.io/gpx/eeadf92b.gpx	경북 구미시 임수동 94-1	2025-05-21 04:50:07+00	2025-05-21 04:51:10+00	0.05195994675159454	0	20.207913967759648	36	11.783332824707031	11.9875	2025-05-21 04:51:16.891017+00
e915161a-46a1-4a59-ab4f-d645ff4deda8	a243b676-df47-42c3-a6cb-b91d60410ffd	\N	66b8e281-9f32-438f-bec3-609a057cb653	https://k12d107.p.ssafy.io/gpx/5a163c41.gpx	경북 구미시 임수동 94-1	2025-05-21 04:50:52+00	2025-05-21 04:51:33+00	0.01131463423371315	0	60.39388000422466	0	17.699999809265137	0	2025-05-21 04:51:35.306749+00
f859bba8-4d94-4b4e-bdc3-4843eb24215a	a886fafd-b8c3-4c81-91af-dc3df66c71bd	\N	8945247e-deb5-4ee3-83ff-face388a645e	https://k12d107.p.ssafy.io/gpx/8593261e.gpx	경북 구미시 임수동 94-1	2025-05-21 04:52:29+00	2025-05-21 04:52:49+00	0.01549326442182064	0	24.74198499533379	105	35.349998474121094	0	2025-05-21 04:52:56.638791+00
be0ec775-d9ff-4dec-b9ef-9b8153c5b030	a243b676-df47-42c3-a6cb-b91d60410ffd	\N	8945247e-deb5-4ee3-83ff-face388a645e	https://k12d107.p.ssafy.io/gpx/66a095cf.gpx	경북 구미시 임수동 94-1	2025-05-21 04:52:06+00	2025-05-21 04:52:56+00	0.033665500581264496	0	24.258313571324713	0	14.15999984741211	0	2025-05-21 04:53:00.773807+00
e70cf129-03ad-4e48-a5fc-4d26ac581188	a886fafd-b8c3-4c81-91af-dc3df66c71bd	\N	3bb51c89-4181-49e3-9321-4c5443c3d079	https://k12d107.p.ssafy.io/gpx/5d951994.gpx	경북 구미시 임수동 94-1	2025-05-21 04:55:51+00	2025-05-21 04:56:43+00	0.03490033000707626	0	25.31022181699918	28.8	14.15999984741211	0	2025-05-21 04:56:50.87414+00
4432eeb4-b372-42ce-865f-c24cf2a6493a	a243b676-df47-42c3-a6cb-b91d60410ffd	\N	3bb51c89-4181-49e3-9321-4c5443c3d079	https://k12d107.p.ssafy.io/gpx/23c5478b.gpx	경북 구미시 임수동 94-1	2025-05-21 04:56:25+00	2025-05-21 04:56:51+00	0.007509773597121239	0	57.70269495618811	0	28.279998779296875	0	2025-05-21 04:56:54.91596+00
10afadf8-91f7-476d-82e8-386121f2fc63	a886fafd-b8c3-4c81-91af-dc3df66c71bd	\N	\N	https://k12d107.p.ssafy.io/gpx/35175947.gpx	경북 구미시 임수동 94-1	2025-05-21 04:59:34+00	2025-05-21 05:02:06+00	0.07070592790842056	0	35.82922341165416	0	4.71999994913737	23.975	2025-05-21 05:02:08.146563+00
b4946894-0acd-4f4f-8b2c-4cfb2f2e1be7	063cf97b-0c40-4c82-a4f7-4f711ea11578	\N	\N	https://k12d107.p.ssafy.io/gpx/22ded255.gpx	경북 구미시 임수동 94-1	2025-05-21 05:03:00+00	2025-05-21 05:03:22+00	0.017357001081109047	0	22.085273127107314	54	35.39999961853027	0	2025-05-21 05:03:29.798492+00
221b1875-0996-4f0e-9932-91d8a15a5946	063cf97b-0c40-4c82-a4f7-4f711ea11578	\N	a0212606-44b6-4ac2-9336-8b43255cacf8	https://k12d107.p.ssafy.io/gpx/30b2c4c1.gpx	경북 구미시 임수동 94-1	2025-05-21 05:04:39+00	2025-05-21 05:05:37+00	0.03180709481239319	0	30.39160248953464	1.0909090909090908	12.872727134011008	0	2025-05-21 05:05:40.529569+00
ffc4a45b-65f8-45a5-9f43-f1cfaf8cce8b	a243b676-df47-42c3-a6cb-b91d60410ffd	\N	a0212606-44b6-4ac2-9336-8b43255cacf8	https://k12d107.p.ssafy.io/gpx/55409f71.gpx	경북 구미시 임수동 94-1	2025-05-21 05:04:54+00	2025-05-21 05:05:45+00	0.033762652426958084	0	25.175796616610537	0	14.139999389648438	0	2025-05-21 05:05:52.566708+00
3b989454-5673-4b06-b52e-453f61a863de	063cf97b-0c40-4c82-a4f7-4f711ea11578	\N	\N	https://k12d107.p.ssafy.io/gpx/c570786a.gpx	경북 구미시 임수동 94-1	2025-05-21 05:16:43+00	2025-05-21 05:17:35+00	0.03235962241888046	0	27.297447078985567	84	14.139999389648438	0	2025-05-21 05:17:45.511448+00
62d8affe-f7f2-4654-8145-f0a6c19f025c	a886fafd-b8c3-4c81-91af-dc3df66c71bd	\N	\N	https://k12d107.p.ssafy.io/gpx/6c74aff7.gpx	경북 구미시 임수동 94-1	2025-05-21 05:21:09+00	2025-05-21 05:21:30+00	0.0022649148013442755	0	9713.3896484375	33	37.21294494760737	0	2025-05-21 05:21:34.437988+00
50a2d53f-e2b2-4f9d-b5c0-b3d675fde793	a886fafd-b8c3-4c81-91af-dc3df66c71bd	\N	\N	https://k12d107.p.ssafy.io/gpx/1c38e454.gpx	경북 구미시 임수동 94-1	2025-05-21 05:22:41+00	2025-05-21 05:22:57+00	0.01120658777654171	0	1427.731689453125	80	49.73174848495074	0	2025-05-21 05:23:00.976083+00
b440a9f5-4f2b-4a38-ada1-f26569955b99	978a211a-d660-404b-b89e-bd115fc48532	\N	b9fc057f-8c3c-43e6-9adf-59a5a5f6b949	https://k12d107.p.ssafy.io/gpx/54b26844.gpx	경북 구미시 임수동 94-1	2025-05-21 05:27:53+00	2025-05-21 05:28:34+00	0.016082733869552612	0	42.488718331521184	0	17.674999237060547	0	2025-05-21 05:28:40.077175+00
1e0deaa1-9e9b-4cd0-bd1d-81d96a9cdbc0	063cf97b-0c40-4c82-a4f7-4f711ea11578	\N	\N	https://k12d107.p.ssafy.io/gpx/9e67f24a.gpx	경북 구미시 임수동 94-1	2025-05-21 05:24:46+00	2025-05-21 05:28:37+00	0.13171909749507904	0	29.608520636925448	0	3.0739129107931387	36.75	2025-05-21 05:28:45.208817+00
49658b6c-cad2-471a-993b-050b7c12f749	a243b676-df47-42c3-a6cb-b91d60410ffd	423fdba7-24ea-41be-8bde-d7ffa61dae00	\N	https://k12d107.p.ssafy.io/gpx/7a5e6285.gpx	경북 구미시 임수동 94-1	2025-05-21 05:33:04+00	2025-05-21 05:33:25+00	0.014549316838383675	0	24.05616074953276	69	35.349998474121094	0	2025-05-21 05:33:27.96708+00
b41eeb53-3f1b-4679-b974-566f7182dd26	a886fafd-b8c3-4c81-91af-dc3df66c71bd	\N	\N	https://k12d107.p.ssafy.io/gpx/a075512c.gpx	경북 구미시 임수동 94-1	2025-05-21 05:44:47+00	2025-05-21 05:44:57+00	0.007535391487181187	0	1592.4852294921875	0	70.9000015258789	0	2025-05-21 05:44:59.729374+00
e61efe63-b286-4e5c-b30a-2fa4ebbced5c	a886fafd-b8c3-4c81-91af-dc3df66c71bd	\N	\N	https://k12d107.p.ssafy.io/gpx/618cb8ef.gpx	경북 구미시 임수동 94-1	2025-05-21 05:48:25+00	2025-05-21 05:48:32+00	0.0011163507588207722	0	6270.4306640625	0	141.8000030517578	0	2025-05-21 05:48:36.524843+00
ce970161-5fe0-4699-bb8a-e3fc54c8b4bd	a243b676-df47-42c3-a6cb-b91d60410ffd	\N	\N	https://k12d107.p.ssafy.io/gpx/31d795d5.gpx	경북 구미시 임수동 94-1	2025-05-21 06:09:10+00	2025-05-21 06:09:21+00	0.005724244751036167	0	32.02758037135023	0	70.9000015258789	0	2025-05-21 06:09:25.81098+00
ecda6f7f-9b04-4fc8-bb67-c16a2ea3de38	a243b676-df47-42c3-a6cb-b91d60410ffd	\N	\N	https://k12d107.p.ssafy.io/gpx/ad66c374.gpx	경북 구미시 임수동 94-1	2025-05-21 06:09:27+00	2025-05-21 06:09:34+00	0.00032490852754563093	0	307.7795584759415	0	141.5999984741211	0	2025-05-21 06:09:37.833444+00
6c1f890f-d3c9-4804-9f8f-447c982fa362	a243b676-df47-42c3-a6cb-b91d60410ffd	\N	594c4d5f-9963-4971-ba74-4091489b041a	https://k12d107.p.ssafy.io/gpx/02d3c6ca.gpx	경북 구미시 임수동 94-1	2025-05-21 06:10:22+00	2025-05-21 06:10:38+00	0.01095656119287014	0	24.338585605878148	0	47.26666768391927	0	2025-05-21 06:10:41.028892+00
7672b3bd-6d7d-4689-a1bc-4a3e049960d7	a243b676-df47-42c3-a6cb-b91d60410ffd	\N	594c4d5f-9963-4971-ba74-4091489b041a	https://k12d107.p.ssafy.io/gpx/15a6dc62.gpx	경북 구미시 임수동 94-1	2025-05-21 06:11:08+00	2025-05-21 06:11:18+00	0.00926796905696392	0	19.781431875672066	0	70.9000015258789	0	2025-05-21 06:11:23.764091+00
33df5675-4cc0-4876-953b-e35d92a02297	a243b676-df47-42c3-a6cb-b91d60410ffd	\N	bb58b720-a7f8-4ca4-8af9-7303b88c5019	https://k12d107.p.ssafy.io/gpx/405b481b.gpx	경북 구미시 임수동 94-1	2025-05-21 06:24:48+00	2025-05-21 06:25:14+00	0.014922400936484337	0	29.03917461641754	0	28.279998779296875	0	2025-05-21 06:25:17.402986+00
02a7ec5c-1b70-4e05-bf7d-86780ee13d56	a243b676-df47-42c3-a6cb-b91d60410ffd	\N	78195365-8d2c-4e9b-a08a-71da62367f42	https://k12d107.p.ssafy.io/gpx/7007e606.gpx	경북 구미시 임수동 94-1	2025-05-21 06:28:30+00	2025-05-21 06:28:47+00	0.022118013352155685	0	12.056562015534558	24	47.26666768391927	0	2025-05-21 06:28:50.235233+00
d3d6cd77-4a2b-4db0-a54d-95305040cede	a243b676-df47-42c3-a6cb-b91d60410ffd	\N	\N	https://k12d107.p.ssafy.io/gpx/b9be9c98.gpx	경북 구미시 임수동 94-1	2025-05-21 06:28:59+00	2025-05-21 06:29:04+00	0.0005881484248675406	0	170.0254561061826	0	141.5999984741211	0	2025-05-21 06:29:09.240885+00
2294e507-f0a5-4024-bff8-2d059abfbc20	a243b676-df47-42c3-a6cb-b91d60410ffd	\N	83c9bedc-b171-4bc6-8455-458e0555d674	https://k12d107.p.ssafy.io/gpx/41421f10.gpx	경북 구미시 임수동 94-1	2025-05-21 06:29:29+00	2025-05-21 06:30:02+00	0.0298141036182642	0	17.329639794516517	0	23.599999745686848	0	2025-05-21 06:30:17.493284+00
dae94339-d220-419d-85f5-a04960435f6e	a243b676-df47-42c3-a6cb-b91d60410ffd	\N	c7fcfb41-c04e-4c4c-bf95-d7a4d5e35e9e	https://k12d107.p.ssafy.io/gpx/96e6e121.gpx	경북 구미시 임수동 94-1	2025-05-21 06:32:25+00	2025-05-21 06:32:41+00	0.009949084371328354	0	26.803190368440692	0	47.133331298828125	0	2025-05-21 06:32:45.765202+00
02b3b343-8851-44d8-976c-a6bfc3326a61	a243b676-df47-42c3-a6cb-b91d60410ffd	\N	78057c76-3a3c-4730-b6a6-b1a64d9bb935	https://k12d107.p.ssafy.io/gpx/ff8839a9.gpx	경북 구미시 임수동 94-1	2025-05-21 06:38:07+00	2025-05-21 06:38:18+00	0.007866217754781246	0	23.30646087720952	0	70.69999694824219	0	2025-05-21 06:38:39.246559+00
c9e127b8-1f37-4e10-98c8-1bb3954be138	a243b676-df47-42c3-a6cb-b91d60410ffd	\N	1b251047-5ca0-4526-b0ac-8b1f5dbbdc80	https://k12d107.p.ssafy.io/gpx/1a868a81.gpx	경북 구미시 임수동 94-1	2025-05-21 06:45:19+00	2025-05-21 06:46:10+00	0.015181818045675755	0	55.98813722703561	5.333333333333333	15.711110432942709	0	2025-05-21 06:46:14.793072+00
d09c4411-b879-4844-8f42-286455e14a09	962c3730-2d81-43fb-a4ac-30e2196a81c3	\N	\N	https://k12d107.p.ssafy.io/gpx/20bc98f3.gpx	경북 구미시 임수동 94-1	2025-05-21 10:16:48+00	2025-05-21 10:17:13+00	0.011948246508836746	0	2343.440185546875	0	28.31999969482422	0	2025-05-21 10:17:16.048846+00
85831466-2929-41b4-bf0f-4815fde1c8ae	a886fafd-b8c3-4c81-91af-dc3df66c71bd	\N	23248a1c-ff7d-4e04-9e54-f72392bc5da6	https://k12d107.p.ssafy.io/gpx/b2bf0208.gpx	경북 구미시 황상동 305-69	2025-05-21 13:04:12+00	2025-05-21 13:05:52+00	0.1356249451637268	0	737.3275756835938	61.8	6.5	2.996875	2025-05-21 13:05:58.41812+00
a380a737-a562-457c-9450-18b3e07a0b04	bb87e298-e9a8-43e1-a528-75082d365036	\N	23248a1c-ff7d-4e04-9e54-f72392bc5da6	https://k12d107.p.ssafy.io/gpx/ae5ffc73.gpx	경북 구미시 임수동 94-1	2025-05-21 13:04:42+00	2025-05-21 13:05:53+00	0.26446810364723206	0	268.46337890625	90	9.299999782017299	11.9875	2025-05-21 13:05:58.741639+00
92c7cf79-48bf-4009-9a65-afc02c0e38df	bb87e298-e9a8-43e1-a528-75082d365036	\N	e7cdafc3-8d99-4911-a17d-ae47dffba80c	https://k12d107.p.ssafy.io/gpx/5eaf7620.gpx	경북 구미시 황상동 304-10	2025-05-21 13:07:30+00	2025-05-21 13:17:06+00	0.8275504112243652	0	696.0301513671875	117.47899159663865	1.099551670697292	26.971875	2025-05-21 13:17:24.342593+00
24083da6-8b8d-4fb8-90d4-962e92a54faa	1206a60d-2489-42c3-b67d-5dfc3948acfe	\N	1d8adb19-cbd3-40de-b305-6c9c1bea6fc3	https://k12d107.p.ssafy.io/gpx/07678bd5.gpx	경북 구미시 황상동 304-10	2025-05-21 13:26:40+00	2025-05-21 13:32:20+00	0.41313424706459045	0	822.9769897460938	94.95652173913044	1.9797101228133491	14.21875	2025-05-21 13:32:26.328749+00
e74d73d8-930f-4ff8-ad8e-039d5b852363	063cf97b-0c40-4c82-a4f7-4f711ea11578	\N	1d8adb19-cbd3-40de-b305-6c9c1bea6fc3	https://k12d107.p.ssafy.io/gpx/ba8450b7.gpx	경북 구미시 임수동 94-1	2025-05-21 13:26:43+00	2025-05-21 13:32:27+00	0.18704108893871307	0	30.741968094412588	0	2.0794116749483	61.25	2025-05-21 13:32:29.751193+00
45623aa7-dd30-4432-a40b-8e9099124f46	a243b676-df47-42c3-a6cb-b91d60410ffd	\N	1d8adb19-cbd3-40de-b305-6c9c1bea6fc3	https://k12d107.p.ssafy.io/gpx/550a2dfb.gpx	경북 구미시 임수동 94-1	2025-05-21 13:28:09+00	2025-05-21 13:32:25+00	0.16267499327659607	0	26.228218965229374	0	2.8279998779296873	37.8	2025-05-21 13:32:29.861872+00
10342806-2f56-48b6-a552-07b9c8a7a559	a886fafd-b8c3-4c81-91af-dc3df66c71bd	\N	\N	https://k12d107.p.ssafy.io/gpx/dbf4d6a5.gpx	경북 구미시 황상동 376-5	2025-05-21 13:36:04+00	2025-05-21 13:38:33+00	0.1767442375421524	0	843.026123046875	113.2	4.573333485921224	5.99375	2025-05-21 13:38:35.473741+00
72edcb26-4901-4e6c-9383-8742871b9b05	417b3d0e-c29e-4840-a043-415f15c45b6b	\N	abaaea07-9eb4-45d1-9eac-ba349b5e3ed5	https://k12d107.p.ssafy.io/gpx/36198e61.gpx	경북 구미시 임수동 94-1	2025-05-21 13:37:48+00	2025-05-21 13:40:36+00	0.09777486324310303	0	28.637275080195806	0	4.284848299893466	18.9	2025-05-21 13:40:39.153047+00
be34f505-f236-443e-b37b-02b55607b5ed	417b3d0e-c29e-4840-a043-415f15c45b6b	\N	\N	https://k12d107.p.ssafy.io/gpx/af39e465.gpx	경북 구미시 임수동 94-1	2025-05-21 13:41:16+00	2025-05-21 13:41:25+00	0.003811793401837349	0	34.979231178134356	0	70.69999694824219	0	2025-05-21 13:41:29.758542+00
7601c6a9-2d3f-49d4-8cf6-555d7ea97233	417b3d0e-c29e-4840-a043-415f15c45b6b	\N	bd43d1e8-6e69-43cb-9029-97fae5d7f081	https://k12d107.p.ssafy.io/gpx/843f5734.gpx	경북 구미시 임수동 94-1	2025-05-21 13:44:14+00	2025-05-21 13:54:48+00	0.407682865858078	0	24.569801368232152	1.2100840336134453	1.1915966643004858	122.85	2025-05-21 13:57:58.798223+00
10c0c21f-ffdb-455e-a5c3-4a266d5e4136	063cf97b-0c40-4c82-a4f7-4f711ea11578	8efc7000-ae53-45eb-9682-61fcfb549050	\N	https://k12d107.p.ssafy.io/gpx/449efe0a.gpx	경북 구미시 임수동 94-1	2025-05-21 14:10:53+00	2025-05-21 14:11:00+00	0.004039830062538385	0	33.00475345269399	0	141.39999389648438	0	2025-05-21 14:11:04.009785+00
101583f6-94d7-4814-9ffe-415788515eb1	a243b676-df47-42c3-a6cb-b91d60410ffd	8333435b-06d1-4706-bea9-1ac2ecd0aceb	\N	https://k12d107.p.ssafy.io/gpx/ff4ba6f1.gpx	경북 구미시 임수동 94-1	2025-05-21 14:31:07+00	2025-05-21 14:31:13+00	0.005218314006924629	0	22.357201485603746	120	141.5999984741211	0	2025-05-21 14:31:19.35854+00
03892000-c320-4b79-b589-ca3955afbe25	962c3730-2d81-43fb-a4ac-30e2196a81c3	\N	\N	https://k12d107.p.ssafy.io/gpx/c55030e9.gpx	경북 구미시 임수동 94-1	2025-05-21 13:34:51+00	2025-05-21 13:45:05+00	0.26227375864982605	0	2344.8779296875	0	1.1915966643004858	69.3	2025-05-21 14:38:35.041201+00
16f5d0b1-f2a8-410b-bfa7-87eae4b07a5c	063cf97b-0c40-4c82-a4f7-4f711ea11578	\N	312bb6e5-ed12-4db1-874e-4ffa119f9dfa	https://k12d107.p.ssafy.io/gpx/fa46a3d7.gpx	경북 구미시 임수동 94-1	2025-05-21 15:04:54+00	2025-05-21 15:05:47+00	0.10959775745868683	0	492.7108459472656	132	13.487122510531805	0	2025-05-21 15:05:52.869782+00
f01e054b-f197-4670-a077-34d83cc78891	063cf97b-0c40-4c82-a4f7-4f711ea11578	\N	\N	https://k12d107.p.ssafy.io/gpx/cce55d6b.gpx	경북 구미시 임수동 94-1	2025-05-21 15:07:32+00	2025-05-21 15:09:23+00	0.13205265998840332	0	848.146484375	57.714285714285715	6.752381097702753	3.0625	2025-05-21 15:09:26.434587+00
d297477d-55c7-4151-976a-14de08c16778	063cf97b-0c40-4c82-a4f7-4f711ea11578	\N	\N	https://k12d107.p.ssafy.io/gpx/e8b657e7.gpx	경북 구미시 임수동 94-1	2025-05-21 15:35:07+00	2025-05-21 15:35:25+00	0.00569709250703454	0	3510.562744140625	0	47.26666768391927	0	2025-05-21 15:35:28.668172+00
59f4382f-bef0-4910-ba8d-0dad58dd9b9e	063cf97b-0c40-4c82-a4f7-4f711ea11578	\N	\N	https://k12d107.p.ssafy.io/gpx/d557e0c2.gpx	경북 구미시 임수동 94-1	2025-05-21 15:35:35+00	2025-05-21 15:36:15+00	0.008802422322332859	0	4657.80859375	0	17.725000381469727	0	2025-05-21 15:36:23.597084+00
3dedb3e2-799d-48f0-a130-ea84259fa474	063cf97b-0c40-4c82-a4f7-4f711ea11578	\N	\N	https://k12d107.p.ssafy.io/gpx/f522ea5b.gpx	경북 구미시 임수동 94-1	2025-05-21 15:36:41+00	2025-05-21 15:36:47+00	0.0014752681599929929	0	6778.4287109375	0	141.8000030517578	0	2025-05-21 15:36:55.754717+00
28b359b0-f928-4b46-b93c-0635490f2980	063cf97b-0c40-4c82-a4f7-4f711ea11578	\N	51796884-b42a-443b-87b3-027e8d01cb05	https://k12d107.p.ssafy.io/gpx/75086de4.gpx	경북 구미시 임수동 94-1	2025-05-21 15:44:25+00	2025-05-21 15:45:29+00	0.027391506358981133	0	2372.998291015625	0	12.872727134011008	1.225	2025-05-21 15:45:40.800119+00
8f6a2b1a-189d-490d-be84-8c88eb31d69b	417b3d0e-c29e-4840-a043-415f15c45b6b	\N	\N	https://k12d107.p.ssafy.io/gpx/eac3544a.gpx	경북 구미시 임수동 94-1	2025-05-21 15:45:31+00	2025-05-21 15:45:39+00	0.0008930502808652818	0	130.63867951100576	0	141.5999984741211	0	2025-05-21 15:45:43.537275+00
242b7ed2-d7c9-49a9-b35b-3a89d6ad998e	417b3d0e-c29e-4840-a043-415f15c45b6b	\N	\N	https://k12d107.p.ssafy.io/gpx/9e49a88b.gpx	경북 구미시 임수동 94-1	2025-05-21 15:45:49+00	2025-05-21 15:46:11+00	0.003787027671933174	0	48.41097543744957	0	70.69999694824219	0	2025-05-21 15:46:13.748283+00
14397a34-1da4-42c1-9ac9-4694d1e8fd2e	417b3d0e-c29e-4840-a043-415f15c45b6b	\N	defaebbe-4c85-4ff3-a5be-71d89c1c4733	https://k12d107.p.ssafy.io/gpx/2e56834b.gpx	경북 구미시 임수동 94-1	2025-05-21 15:46:57+00	2025-05-21 15:47:47+00	0.01101883128285408	0	51.42721520248877	0	23.633333841959637	0	2025-05-21 15:47:49.425674+00
b5060880-7f4d-4bd5-a83c-cc255f3c1873	6ffb74df-9bc4-41ed-b2b8-2b949e07f0b2	\N	\N	https://k12d107.p.ssafy.io/gpx/0ce4b55e.gpx	경북 구미시 임수동 94-1	2025-05-21 15:47:29+00	2025-05-21 15:47:56+00	0.005176396109163761	0	90.15299426291149	0	28.31999969482422	0	2025-05-21 15:48:13.714161+00
f7af3daf-3f1e-4b9b-9223-b92475d8d5b7	6ffb74df-9bc4-41ed-b2b8-2b949e07f0b2	\N	29812557-16d3-48c8-8245-19b2b19daa75	https://k12d107.p.ssafy.io/gpx/f76b8c7a.gpx	경북 구미시 임수동 94-1	2025-05-21 15:48:34+00	2025-05-21 15:49:43+00	0.03449929133057594	0	15.459286054617415	42	23.633333841959637	0	2025-05-21 15:51:01.756822+00
1b8e9d05-def3-45a7-b213-aeac74fe2450	063cf97b-0c40-4c82-a4f7-4f711ea11578	\N	\N	https://k12d107.p.ssafy.io/gpx/cd246b05.gpx	경북 구미시 임수동 94-1	2025-05-21 16:04:07+00	2025-05-21 16:11:41+00	0.17441518604755402	0	2333.51220703125	0	2.3213114503954277	7.35	2025-05-21 16:11:43.970679+00
a4fc4cf1-0319-49ca-bb45-243ece7afd9e	0f9c3869-310c-48c5-b838-c50fa0ad5bc1	\N	c98493af-00a7-4e95-934d-474c61e8944d	https://k12d107.p.ssafy.io/gpx/5861790c.gpx	경북 구미시 임수동 94-1	2025-05-21 16:17:13+00	2025-05-21 16:22:15+00	0.1013379916548729	0	46.21507563761848	0	2.5709089799360796	47.95	2025-05-21 16:22:18.749822+00
ec71accb-3dd4-4a04-9e97-c2175687e646	a886fafd-b8c3-4c81-91af-dc3df66c71bd	8cea1f49-4539-4679-9ed2-6afc76236c30	1d8adb19-cbd3-40de-b305-6c9c1bea6fc3	https://k12d107.p.ssafy.io/gpx/f719e162.gpx	경북 구미시 황상동 304-4	2025-05-21 13:26:39+00	2025-05-21 13:32:19+00	0.409357488155365	0	830.5698852539062	92.6470588235294	2.0014705657958984	14.984375	2025-05-21 13:32:23.500897+00
3156d77a-7374-4d1c-b6af-e30545bb0519	a886fafd-b8c3-4c81-91af-dc3df66c71bd	272e47c5-76ad-4113-8f9d-555156adfb5e	e7cdafc3-8d99-4911-a17d-ae47dffba80c	https://k12d107.p.ssafy.io/gpx/d6dc5032.gpx	경북 구미시 황상동 304-10	2025-05-21 13:07:30+00	2025-05-21 13:17:14+00	0.8751320838928223	0	668.4705200195312	97.5126050420168	1.0739495413643974	26.971875	2025-05-21 13:17:22.73039+00
7f6ead47-bd8f-4d5e-8b43-ba2296b0bd80	978a211a-d660-404b-b89e-bd115fc48532	\N	\N	https://k12d107.p.ssafy.io/gpx/12c8f759.gpx	경북 구미시 임수동 94-1	2025-05-21 16:51:45+00	2025-05-21 16:52:34+00	0.024537494406104088	0	25.131657328878156	0	20.25714329310826	0	2025-05-21 16:52:40.197896+00
13bb84cc-8702-46f0-b428-4c21fec3281c	978a211a-d660-404b-b89e-bd115fc48532	\N	b0bbb42c-6588-47ef-a9ad-1117871d6f00	https://k12d107.p.ssafy.io/gpx/c9547e58.gpx	경북 구미시 임수동 94-1	2025-05-21 16:52:50+00	2025-05-21 16:53:10+00	0.006267500575631857	0	61.162199961918645	0	35.39999961853027	0	2025-05-21 16:53:17.382624+00
95873b51-02b4-498c-8a5c-90bf2d5e43b1	0f9c3869-310c-48c5-b838-c50fa0ad5bc1	\N	b0bbb42c-6588-47ef-a9ad-1117871d6f00	https://k12d107.p.ssafy.io/gpx/6d4300e5.gpx	경북 구미시 임수동 94-1	2025-05-21 16:52:33+00	2025-05-21 16:53:13+00	0.015278475359082222	0	37.0892893827746	0	20.199999128069198	0	2025-05-21 16:53:17.625165+00
79df2ad8-9bc1-434a-9f27-3638a558c0c6	0f9c3869-310c-48c5-b838-c50fa0ad5bc1	\N	0aa89867-6592-4cf1-bca7-e567798cc2c5	https://k12d107.p.ssafy.io/gpx/c36af8a2.gpx	경북 구미시 임수동 94-1	2025-05-21 16:56:20+00	2025-05-21 16:56:32+00	0.0025079690385609865	0	86.39145526928158	0	70.69999694824219	0	2025-05-21 16:56:34.609289+00
e7669c18-6e6f-428d-890f-8a1af1863fa2	a243b676-df47-42c3-a6cb-b91d60410ffd	\N	0aa89867-6592-4cf1-bca7-e567798cc2c5	https://k12d107.p.ssafy.io/gpx/b5c4a309.gpx	경북 구미시 임수동 94-1	2025-05-21 16:56:15+00	2025-05-21 16:56:27+00	0.008068653754889965	0	26.85294325204873	0	70.69999694824219	0	2025-05-21 16:56:34.843464+00
cd8a11bd-eabe-49e1-814f-c30be0e3a94c	6ffb74df-9bc4-41ed-b2b8-2b949e07f0b2	\N	\N	https://k12d107.p.ssafy.io/gpx/e30dc924.gpx	경북 구미시 임수동 94-1	2025-05-21 16:50:23+00	2025-05-21 16:50:38+00	0.004902958869934082	0	61.18766252466963	8	47.133331298828125	0	2025-05-21 17:15:50.92188+00
d773177f-a505-484f-bb74-eca63fd6cf5c	a886fafd-b8c3-4c81-91af-dc3df66c71bd	\N	\N	https://k12d107.p.ssafy.io/gpx/6a5db977.gpx	경북 구미시 인의동 843-3	2025-05-21 18:46:56.174+00	2025-05-21 18:47:25.46+00	0.009975831717252732	0	0	52	65.9000015258789	20603184272806.45	2025-05-21 18:47:53.580427+00
857309d9-cb2c-452f-9f77-b45712a539ce	a886fafd-b8c3-4c81-91af-dc3df66c71bd	\N	\N	https://k12d107.p.ssafy.io/gpx/e76c12e9.gpx	경북 구미시 인의동 843-3	2025-05-21 19:05:04.198+00	2025-05-21 19:08:50.043+00	0.19041494283080102	102	591.0526315789474	98	66.27446163762848	10476028486083.387	2025-05-21 19:09:58.5433+00
4c2ea0f7-3008-453e-9cf4-f8eca7ed8898	a886fafd-b8c3-4c81-91af-dc3df66c71bd	\N	\N	https://k12d107.p.ssafy.io/gpx/03352f16.gpx	경북 구미시 인의동 843-3	2025-05-21 19:26:39.598+00	2025-05-21 19:27:49.668+00	0.07225604736804962	82	29.083333333333332	99	65.90666809082032	11.9875	2025-05-21 19:30:50.518894+00
bdb8e72f-e9c0-4458-adbf-4fa048e2e1e8	a886fafd-b8c3-4c81-91af-dc3df66c71bd	\N	\N	https://k12d107.p.ssafy.io/gpx/8b4daf18.gpx	경북 구미시 인의동 843-3	2025-05-21 19:15:23.234+00	2025-05-21 19:18:33.838+00	0.17668464617431165	88	293.8888888888889	39	65.91794996995192	35.9625	2025-05-21 19:30:50.667095+00
49bd0c94-12a1-4c10-ac44-12d94566e813	a886fafd-b8c3-4c81-91af-dc3df66c71bd	\N	\N	https://k12d107.p.ssafy.io/gpx/7ae88edf.gpx	경북 구미시 인의동 843-3	2025-05-21 19:31:08.817+00	2025-05-21 19:31:39.082+00	0.03006875550746918	79	42.5	42	65.91428702218192	0	2025-05-21 19:32:01.839914+00
2ad460f2-1714-4819-a863-ac29da569f6f	a886fafd-b8c3-4c81-91af-dc3df66c71bd	\N	\N	https://k12d107.p.ssafy.io/gpx/4282e99e.gpx	경북 구미시 인의동 843-3	2025-05-21 19:41:33.373+00	2025-05-21 19:41:58.233+00	0.02279671823978424	92	46	85	66.4926434096248	0	2025-05-21 19:42:17.491346+00
dbd11fea-b6c1-409f-ae19-a84508ae84e9	a886fafd-b8c3-4c81-91af-dc3df66c71bd	\N	\N	https://k12d107.p.ssafy.io/gpx/52d30011.gpx	경북 구미시 인의동 843-3	2025-05-21 19:45:15.136+00	2025-05-21 19:46:14.842+00	0.060811980724334726	78	30.555555555555557	31	65.9000015258789	11.928761250000003	2025-05-21 19:52:26.320297+00
8d71919c-b6ef-4fcd-8c60-228d47cfd4f8	a886fafd-b8c3-4c81-91af-dc3df66c71bd	\N	\N	https://k12d107.p.ssafy.io/gpx/d87fc5aa.gpx	경북 구미시 인의동 843-3	2025-05-21 19:49:58.114+00	2025-05-21 19:50:28.19+00	0.023340313464403153	0	1211	79	65.9000015258789	0.6008934166666666	2025-05-21 19:52:26.398867+00
1c63eb54-b525-42ce-a377-4f8834cd8c98	a886fafd-b8c3-4c81-91af-dc3df66c71bd	\N	\N	https://k12d107.p.ssafy.io/gpx/bb17eeff.gpx	경북 구미시 인의동 843-3	2025-05-21 19:48:14.225+00	2025-05-21 19:48:44.623+00	0.03320476269721985	83	20	0	65.9000015258789	6.0732670833333335	2025-05-21 19:52:26.400044+00
924d5812-1824-4693-b18a-f4737de9cc56	a886fafd-b8c3-4c81-91af-dc3df66c71bd	\N	\N	https://k12d107.p.ssafy.io/gpx/c688a200.gpx	경북 구미시 인의동 843-3	2025-05-21 19:53:12.726+00	2025-05-21 19:53:37.714+00	0.015269411325454712	67	19	76	65.9000015258789	4.992394166666666	2025-05-21 19:54:03.153036+00
c1b0385a-1f8d-4a0f-8ebc-6309c387906a	a886fafd-b8c3-4c81-91af-dc3df66c71bd	\N	\N	https://k12d107.p.ssafy.io/gpx/b331da3c.gpx	경북 구미시 인의동 843-3	2025-05-21 20:54:17.254+00	2025-05-21 20:54:37.305+00	0.015907737374305723	74	56	0	65.9000015258789	4.006022708333333	2025-05-21 20:55:08.34056+00
4469c3e2-baa5-4941-a9cc-04c45fbc6ce1	a243b676-df47-42c3-a6cb-b91d60410ffd	\N	\N	https://k12d107.p.ssafy.io/gpx/53ba13e3.gpx	경북 구미시 임수동 94-1	2025-05-22 00:12:24+00	2025-05-22 00:12:29+00	0.0022768499329686165	0	2635.2197265625	0	141.39999389648438	0	2025-05-22 00:12:33.95661+00
b7449aff-620d-4ef2-a97c-07c986df0c9b	6ffb74df-9bc4-41ed-b2b8-2b949e07f0b2	\N	f8b1748a-22e6-410e-bd68-993eed5b6625	https://k12d107.p.ssafy.io/gpx/b4454e50.gpx	경북 구미시 임수동 94-1	2025-05-22 00:23:25+00	2025-05-22 00:23:32+00	0.0011859575752168894	0	6745.60400390625	0	141.39999389648438	0	2025-05-22 00:23:38.821017+00
8e0a04b1-adfb-4f9a-96d6-c9b0e8534f09	063cf97b-0c40-4c82-a4f7-4f711ea11578	\N	5b6fa10c-3d64-4498-afa9-9a61758ac47a	https://k12d107.p.ssafy.io/gpx/070ce025.gpx	경북 구미시 황상동 304-2	2025-05-22 00:53:56+00	2025-05-22 00:54:29+00	0.11216383427381516	0	303.12799072265625	159.42857142857142	18.514286586216517	0	2025-05-22 00:54:35.788439+00
5e43a6cd-6378-41a3-aa5b-8c97841ab667	1206a60d-2489-42c3-b67d-5dfc3948acfe	\N	5b6fa10c-3d64-4498-afa9-9a61758ac47a	https://k12d107.p.ssafy.io/gpx/e058e191.gpx	경북 구미시 황상동 303-17	2025-05-22 00:54:28+00	2025-05-22 00:54:56+00	0.08118882030248642	0	357.1920471191406	78	22.100001017252605	0	2025-05-22 00:54:59.405752+00
df17d2bc-39c1-4840-a638-4c36da5d7852	6d084e9d-d896-45d1-af00-1f5063415366	272e47c5-76ad-4113-8f9d-555156adfb5e	\N	https://k12d107.p.ssafy.io/gpx/ccd0baa3.gpx	경북 구미시 임수동 94-1	2025-05-22 01:49:35+00	2025-05-22 01:49:51+00	0.009110774844884872	0	1756.162353515625	24	47.26666768391927	0	2025-05-22 01:49:59.784242+00
e9ea2a93-0928-4cf3-b6b7-352056523bc1	6d084e9d-d896-45d1-af00-1f5063415366	\N	\N	https://k12d107.p.ssafy.io/gpx/8a121b06.gpx	경북 구미시 임수동 94-1	2025-05-22 02:05:39+00	2025-05-22 02:06:11+00	0.005678047891706228	0	5635.73974609375	24	23.566665649414062	0	2025-05-22 02:06:14.768131+00
33eecec6-974a-42a8-a4a5-5d7b72bca4cf	6d084e9d-d896-45d1-af00-1f5063415366	8cea1f49-4539-4679-9ed2-6afc76236c30	\N	https://k12d107.p.ssafy.io/gpx/e7e3fa4d.gpx	경북 구미시 임수동 94-1	2025-05-22 02:06:36+00	2025-05-22 02:06:46+00	0.02250460907816887	0	444.3534240722656	168	70.79999923706055	0	2025-05-22 02:06:50.478912+00
cee08144-639a-4777-948b-f66823237aac	6d084e9d-d896-45d1-af00-1f5063415366	935350bc-261d-4cf2-a39b-a5a49ddaf9a8	\N	https://k12d107.p.ssafy.io/gpx/f583cb9e.gpx	경북 구미시 임수동 94-1	2025-05-22 02:06:57+00	2025-05-22 02:07:13+00	0.04124416038393974	0	387.9337158203125	256	47.133331298828125	0	2025-05-22 02:07:16.950462+00
22c7ff7e-f4a4-401f-9899-baa6613c8af8	6d084e9d-d896-45d1-af00-1f5063415366	0208d32b-650e-469e-a120-a40b0023db65	\N	https://k12d107.p.ssafy.io/gpx/aecdcd8c.gpx	경북 구미시 임수동 94-1	2025-05-22 02:09:00+00	2025-05-22 02:09:04+00	0.0002607923233881593	0	23006.8125	72	141.39999389648438	0	2025-05-22 02:09:10.505895+00
\.


--
-- TOC entry 3497 (class 0 OID 26110)
-- Dependencies: 223
-- Data for Name: marathon_distances; Type: TABLE DATA; Schema: public; Owner: runhwani
--

COPY public.marathon_distances (distance_id, marathon_id, distance) FROM stdin;
9524ad2a-e8c2-4aa3-918b-24125ca16957	436d4548-5bee-4052-b680-ebff7e6d27d4	하프(21.0975km)
f964d0db-649e-4303-9344-30a2e47eb98e	436d4548-5bee-4052-b680-ebff7e6d27d4	10km
7ee3760e-cd00-4594-b044-fb3a7bc0ff5b	436d4548-5bee-4052-b680-ebff7e6d27d4	5km
e08be910-c5ac-4caf-8906-77bcb91db7c3	13d49c17-ca87-453e-9b21-d013d31bfc26	5km
a7cf561d-7ba8-479b-9cac-fa1b343e896b	13d49c17-ca87-453e-9b21-d013d31bfc26	10km
2ab06b9d-b931-42d6-8783-a7111ecd6bf9	e608bf56-0ee9-4511-9668-25278a826529	풀(42.195km)
227c7fe9-91c5-4594-9d84-4f931c70f3b5	e608bf56-0ee9-4511-9668-25278a826529	하프(21.0975km)
11ab5816-0e61-4297-bee9-fb0b59fbb55b	e608bf56-0ee9-4511-9668-25278a826529	10km
71d3f2ec-49e3-42a6-818d-2670832df035	e608bf56-0ee9-4511-9668-25278a826529	5km
7fa18f98-e8b1-46ae-ae68-9781814286e5	be568422-fee9-4caa-9669-06b7791cdbf5	14km
aceb9df6-b790-4e4a-9c7f-186ed7cd0c8c	02950fae-2e6c-4128-a2e6-302bcfdbd391	하프(21.0975km)
a0b393a4-bdf8-43c7-9eba-915754e801d6	02950fae-2e6c-4128-a2e6-302bcfdbd391	10km
1a5e6fd7-684e-4854-ab8c-64b094756d86	72d76a0f-693a-4243-b98c-3242d970d455	하프(21.0975km)
c1f7ab15-3997-4f93-9338-ba13e8844f6c	72d76a0f-693a-4243-b98c-3242d970d455	10km
ecda9b5f-163d-467e-b2fc-25ca8f1fece4	72d76a0f-693a-4243-b98c-3242d970d455	5km
43d095e8-64aa-43eb-9d09-94fecd7c8ce1	97e07f48-d254-40d6-b1ea-2fd95c1cfa37	10km
cafef40b-6b15-42eb-ad80-b4ce872f9aef	97e07f48-d254-40d6-b1ea-2fd95c1cfa37	5km
936056b5-ef02-4bbf-9ea5-6efe7ad51cda	0bdec990-e127-4cb5-9d3a-e309e33923c1	하프(21.0975km)
8f1957bc-69e3-4b6b-a478-02b7b1db0ef4	0bdec990-e127-4cb5-9d3a-e309e33923c1	10km
35844b67-e2f3-4600-811e-48433c75867b	0bdec990-e127-4cb5-9d3a-e309e33923c1	5km
98ead9e8-c37b-466b-b1e3-58306147549a	438963b0-7ceb-4a3a-89f2-339ad40aee65	10km
ba15d5e4-16f3-467e-a949-f87badd89abd	438963b0-7ceb-4a3a-89f2-339ad40aee65	5km
d383114a-0d4c-4ee7-8f46-ab3f44ec7845	b293ebfa-6f4d-4318-a7b1-03780cee67a9	하프(21.0975km)
2eb6d15d-c1e8-4b29-839b-a2c9a886f33d	b293ebfa-6f4d-4318-a7b1-03780cee67a9	10km
b0347132-aa19-481a-9ed7-54ae3e4432bd	b293ebfa-6f4d-4318-a7b1-03780cee67a9	5km
4828ada6-609e-42d2-9255-b7d9d7eaf7c1	42f754eb-cfc9-44df-a066-a770a88671bc	50km.
c461af00-585f-480d-88f1-3b8b438deed3	42f754eb-cfc9-44df-a066-a770a88671bc	100km
77b90310-26af-4478-83e4-5a3bd7f23b86	92d476c4-51ec-462c-87d8-1457c2d33327	하프(21.0975km)
66506729-1321-4c2b-b51f-99dbb26f1ef9	92d476c4-51ec-462c-87d8-1457c2d33327	10km
486921f2-5654-428a-8d5f-9b9d8f12861d	92d476c4-51ec-462c-87d8-1457c2d33327	5km
73785894-6dcd-4d6f-8a6e-91d33b1fd999	6995f3e2-3315-4419-9b08-5992aca95820	하프(21.0975km)
f0795fbc-494c-48e0-aa33-9e0dbdc632bd	6995f3e2-3315-4419-9b08-5992aca95820	10km
9237aec7-afc5-472d-be76-40f59a2b9263	6995f3e2-3315-4419-9b08-5992aca95820	5km
8cb48de7-210b-46f9-835d-3bcde44da862	8e0cd2a4-03fb-4cac-9150-1c2ff3965ba8	하프(21.0975km)
2c581cf5-091e-4738-a855-73a423be0e87	8e0cd2a4-03fb-4cac-9150-1c2ff3965ba8	10km
a2c9a9d8-1ee1-41e3-ac1f-31b605765c6a	8e0cd2a4-03fb-4cac-9150-1c2ff3965ba8	5km
dbfad9f8-8afd-465a-bc9a-32bd8e0e977d	f9aba6df-35f6-4fbe-bbcf-7a4f3c5871aa	30km
cf0d8ff2-1a1a-49bd-b948-ccfe6075d090	f9aba6df-35f6-4fbe-bbcf-7a4f3c5871aa	하프(21.0975km)
fe71fe3f-f23d-48fa-b2eb-9b3f6122b669	f9aba6df-35f6-4fbe-bbcf-7a4f3c5871aa	10km
66191b5f-dd27-48f8-9093-684b63268470	f9aba6df-35f6-4fbe-bbcf-7a4f3c5871aa	5km
e3601652-d52f-4f5f-ab73-e1e0c49d8822	1291a67e-30aa-41cc-a32e-577c1584a963	풀(42.195km)
fd9072fa-6524-46bb-89a0-892a1f143a6c	1291a67e-30aa-41cc-a32e-577c1584a963	하프(21.0975km)
be4e553f-b47e-4f99-b600-a3a2b224b118	1291a67e-30aa-41cc-a32e-577c1584a963	10km
fdcf216d-8649-4404-b3c3-32857a799db6	1291a67e-30aa-41cc-a32e-577c1584a963	5km
5190e959-ee06-4d86-b107-f13a57e4cef4	1c845ba9-8928-432c-82e3-c6e6af11b70b	하프(21.0975km)
f72fae4f-1b1c-4cb2-b572-025c0115f24e	1c845ba9-8928-432c-82e3-c6e6af11b70b	10km
276874ca-91e4-487c-a874-0de151865f68	1c845ba9-8928-432c-82e3-c6e6af11b70b	5km
33cc77e5-3eb6-4685-a8b0-95e12e1dcfb0	b1812afd-276a-4ad7-be49-b301a99d324f	하프(21.0975km)
5ff821df-d403-476f-a96c-2cdc603de3f2	b1812afd-276a-4ad7-be49-b301a99d324f	10km
799849f5-2e73-458f-b953-f1f88376ece5	b1812afd-276a-4ad7-be49-b301a99d324f	5km
c4105f52-28d5-4b42-ab42-74845de2967f	74118f85-1de9-453c-b5ce-004a809ece2b	풀(42.195km)
57f781f6-e9f7-4bf5-b423-658452088a43	74118f85-1de9-453c-b5ce-004a809ece2b	10km
f8a0a149-d365-4065-bac7-b3c1e0749690	2b4b75fb-9c43-4701-9741-5dc9227703db	하프(21.0975km)
eb252109-7b93-4916-b771-c440e09107c0	2b4b75fb-9c43-4701-9741-5dc9227703db	10km
69d93a82-d6a9-48ba-bd27-2cb988a13141	2b4b75fb-9c43-4701-9741-5dc9227703db	5km
b1cc2c42-e383-41fe-9b9b-3c601e51d504	078f5822-068f-42e4-9742-a845d6a4a816	하프(21.0975km)
cdd7285a-bc1c-4e2a-9d33-21900f9f03d3	078f5822-068f-42e4-9742-a845d6a4a816	10km
428ce2d7-44ab-435a-9cc8-eed6dd42ea34	078f5822-068f-42e4-9742-a845d6a4a816	5km
e079b9b0-76e8-4a55-99a4-97da0cb3953c	19209c85-d927-483c-92fe-3004d566e6a7	23.5km
8ebde270-7af8-45a1-9b7a-ba8126a58a48	19209c85-d927-483c-92fe-3004d566e6a7	13km
3dc978ad-9bef-4e0e-b5e1-4d4cc5fad8de	db9ce38d-41a0-4716-8479-cc37766f4c6f	10km
1bd70d7d-33a8-4929-afdf-9effd1c7db04	ac2b9a84-84b4-49d0-ab1d-141d2b977ea3	10km
897faa7e-db13-43ba-8ebd-806ced8c722d	33423f3e-f32f-4d0f-871d-80641d434b66	10km
d18dfe84-5bcf-4697-a992-ce26ffad983b	33423f3e-f32f-4d0f-871d-80641d434b66	5km
ad0ab261-2768-4f11-a2ba-076195d8cd55	7bd901df-eb7f-4292-a0ed-31c489d94270	16km
de8b72a3-58a0-4da3-a177-f91f64859b2c	7bd901df-eb7f-4292-a0ed-31c489d94270	8km
6f0652a3-b527-4754-a45b-fbfa6f15b6e1	97b763cf-1123-4b14-893b-4d2917801203	10km
80bc3c57-2606-4c80-af41-12b0e82300ae	97b763cf-1123-4b14-893b-4d2917801203	5km
dd80d96a-ca98-4417-bb7e-f7f483f816a5	740bc1c4-93ba-489f-bf7d-28ca3ea201a3	풀(42.195km)
b7199ca2-bc4b-46af-92b6-5b950bd8e81f	740bc1c4-93ba-489f-bf7d-28ca3ea201a3	하프(21.0975km)
d570b686-0272-4d83-b123-839d6cd0542e	740bc1c4-93ba-489f-bf7d-28ca3ea201a3	10km
08e0c7d5-5674-4678-9525-e4c90bb5f3a4	740bc1c4-93ba-489f-bf7d-28ca3ea201a3	5km
8b0beecb-044f-40eb-9a57-489d47c40316	df9a46f1-b5e3-456f-8d88-c6375451034f	16km
61868310-175b-4d79-b57e-9996fb98daac	df9a46f1-b5e3-456f-8d88-c6375451034f	10km
9bf2b5f5-0fd2-48ba-a705-939c13b66f57	60ff3524-3777-4f70-883a-46e2c25fe99d	풀(42.195km)
e59a1fe7-8b23-4848-8c47-e938895cc81e	60ff3524-3777-4f70-883a-46e2c25fe99d	하프(21.0975km)
f9c8f771-3b44-407d-8bca-c03a28ee868c	60ff3524-3777-4f70-883a-46e2c25fe99d	10km
22ef8da8-5446-4baa-8929-be074c1720db	60ff3524-3777-4f70-883a-46e2c25fe99d	5km
8cc8ad13-495b-4873-b3ab-c8c7fc6415e0	49e6cd47-bfc1-4d61-9c44-94530c2ef12a	10km
5494ac6f-aa06-452b-bdba-aa4c12bd2e32	49e6cd47-bfc1-4d61-9c44-94530c2ef12a	5km
2b719315-07c9-4878-89f3-081e850b6082	62121e18-92d3-4197-8431-0450724f7ffd	하프(21.0975km)
de36a852-f539-4e99-b731-ac61090c07e9	62121e18-92d3-4197-8431-0450724f7ffd	10km
6ee7cacb-e39e-44a3-bb79-d6096853c21a	62121e18-92d3-4197-8431-0450724f7ffd	5km
74d68504-f069-4e48-9122-52cdd046030f	eba86a64-699d-457c-97d6-429fcc159325	하프(21.0975km)
dbb0b581-c4ff-4f74-9861-54e5f808930d	eba86a64-699d-457c-97d6-429fcc159325	10km
b663ad06-60c3-4807-bb81-3930b7afd083	eba86a64-699d-457c-97d6-429fcc159325	5km
70a6d7da-c6fb-4003-b56c-d335660ee0d9	bf07af3f-97e8-4c72-8012-7d56c362bfcd	10km
77a700b2-6150-499a-834b-ef79d26dcfe9	bf07af3f-97e8-4c72-8012-7d56c362bfcd	5km
aec56ec1-bead-4a2c-8a0a-f6b1d4626e2e	076e57fd-5809-4c36-bdf5-c63dbd4cd846	풀(42.195km)
a5d3e55d-bc58-4bee-9f99-bf9816663f6a	076e57fd-5809-4c36-bdf5-c63dbd4cd846	하프(21.0975km)
8fe6e439-bd7c-40f6-a1aa-1d937e0ba3f7	076e57fd-5809-4c36-bdf5-c63dbd4cd846	10km
8bb3c6b8-0527-4d33-bdeb-d418b8f441e9	076e57fd-5809-4c36-bdf5-c63dbd4cd846	5km
b6c836e6-7715-4b35-acd1-ed4a905d6322	fed0bd70-c78d-46a1-bdff-0197396c268d	하프(21.0975km)
3fad45da-50fb-4a0f-9b2e-91831959a42c	fed0bd70-c78d-46a1-bdff-0197396c268d	10km
d43bec66-89cf-4b81-8e92-dadf9fbcc26b	fed0bd70-c78d-46a1-bdff-0197396c268d	5km
9c73a85c-0473-4169-bd16-0af8e5df2f22	3cfc3332-6868-4666-94a6-72aa627e5967	하프(21.0975km)
c01cb27a-ae6d-44d5-987d-9c67e154418a	3cfc3332-6868-4666-94a6-72aa627e5967	10km
4cb7485a-aed7-41c3-a8b1-257eae6fa7e6	3cfc3332-6868-4666-94a6-72aa627e5967	5km
cc1fcfe7-0262-4d80-82ef-427a3bebf6f3	e724f721-0f9f-46c9-83b2-6b2f2f58364a	10km
fbe34c32-dc2f-4be2-b2f2-7f3609dcbc99	e724f721-0f9f-46c9-83b2-6b2f2f58364a	5km
cf4ccad8-9b15-41ae-92af-d4afd924d44b	e724f721-0f9f-46c9-83b2-6b2f2f58364a	4km걷기
9cfaee42-100b-46ac-b49b-57edc9e03aec	854b87cc-a6a3-4c98-844f-8ffb8c6d67b7	5km
2de5d961-c395-4fab-a4bf-9fe443715e52	ca0be125-6304-4ac4-ab94-c273a5988c54	하프(21.0975km)
25f5cc6c-f642-4c97-be1b-e01d5b93e524	ca0be125-6304-4ac4-ab94-c273a5988c54	10km
ab661485-17f1-4bec-a193-0391d57520a5	ca0be125-6304-4ac4-ab94-c273a5988c54	5km
9b8b8376-196c-4224-8236-6dfeb7fdbf98	c62c8fe5-2583-4c1e-9c21-7670e3cb18e5	10km
b85cff7f-e632-48e1-85ce-9c45c59171da	c62c8fe5-2583-4c1e-9c21-7670e3cb18e5	8km
6e62b6c6-e7c7-48b1-bce0-3b0354f347e7	c62c8fe5-2583-4c1e-9c21-7670e3cb18e5	3km
d8a6cc62-3b34-47ac-9f23-80f8c397bab8	6721a9af-1ae5-4af7-8f73-77a56aa2fccf	10km
2af3dd65-d849-4f02-9744-a6b98e370498	6721a9af-1ae5-4af7-8f73-77a56aa2fccf	5km
e957bb70-f9a4-4d86-a413-dae0cf336bbf	5793599d-8863-4b2a-97ca-8d7b176b9ee5	10km
a623ed00-2b2e-4298-b691-643bc80daf26	5793599d-8863-4b2a-97ca-8d7b176b9ee5	5km
c63b17a7-4672-4d2e-a0cd-2273a2b446e2	cb3f2398-ce3c-4f0e-a77e-06a51f7442d2	하프(21.0975km)
4103014d-3983-4e75-9bb6-ff4118071ae7	cb3f2398-ce3c-4f0e-a77e-06a51f7442d2	10km
427c5da9-2716-45fa-b1ba-a08e330dbf9c	cb3f2398-ce3c-4f0e-a77e-06a51f7442d2	5km
061dbd92-05e8-431f-ae60-4dc0e3bedf50	6934bfde-8b72-4180-becb-0e38eabc5493	풀(42.195km)
8ece9f10-402f-4cd8-94a0-6c2942f190cd	6934bfde-8b72-4180-becb-0e38eabc5493	하프(21.0975km)
6a849963-e5e2-4b8a-a8d9-82a719e4953d	6934bfde-8b72-4180-becb-0e38eabc5493	10km
2adde78b-22a7-4397-8d35-4b7fca295a3c	6934bfde-8b72-4180-becb-0e38eabc5493	5km
2ba07584-ca3c-403e-bdbc-29940f9c8dcc	6173f494-e5e7-4e12-be35-5ddebab1e2d5	하프(21.0975km)
ad16daa3-0718-4ab2-9543-176ba04c3cdd	6173f494-e5e7-4e12-be35-5ddebab1e2d5	10km
c54d0391-6c4f-4437-a6f3-d81ce449b175	6173f494-e5e7-4e12-be35-5ddebab1e2d5	5km
17675836-a1be-4aa2-83ac-ab10400a8395	bc667145-2dfe-4c07-961b-deb3bc9074a0	20km
fe975914-8594-40dc-8f2a-1642d88c946a	bc667145-2dfe-4c07-961b-deb3bc9074a0	15.6km
371ca70b-921b-46c7-934e-f08c6559495b	eb9eaed7-532a-4afc-8d18-254329967912	풀(42.195km)
4bc56315-fb60-457f-bcbb-9d1164b6018a	eb9eaed7-532a-4afc-8d18-254329967912	하프(21.0975km)
75017124-401e-441d-9d3a-b631c930cda9	eb9eaed7-532a-4afc-8d18-254329967912	10km
70d736b7-6c55-4943-b40e-f84f83390896	eb9eaed7-532a-4afc-8d18-254329967912	5km
fc756c4b-a1cd-4517-b97a-f4f5b8ada50f	b454082c-1d0a-49e2-ba65-ae4bdd332c42	10km
c5c0d469-44d1-40ee-a499-0ab0ed0c631b	b454082c-1d0a-49e2-ba65-ae4bdd332c42	5km
e673c322-c163-4187-86ce-076c517743d2	49e65e42-44aa-4c39-a975-16efb7e28c04	하프(21.0975km)
946e57af-5767-4c59-9ce9-ee275b690f23	49e65e42-44aa-4c39-a975-16efb7e28c04	10km
b429e308-530c-4ef9-a0d8-f6a6f55c1d65	49e65e42-44aa-4c39-a975-16efb7e28c04	5km
7f66d8b1-c974-41e4-be84-838f35eab57b	ee149d7c-b911-457d-b0fb-7dd2f6263c63	하프(21.0975km)
51110e26-3c56-4158-9211-00ba28ac8d2a	ee149d7c-b911-457d-b0fb-7dd2f6263c63	10km
4233c744-c9f7-4e27-bd27-a805f82858aa	ee149d7c-b911-457d-b0fb-7dd2f6263c63	5km
8266afad-8c08-41e5-beeb-8b1ea06309ac	4b21a4c9-a706-4b95-b387-f08ba97e2dd0	하프(21.0975km)
d64b4912-7019-424f-ba50-8e8085c2e46d	4b21a4c9-a706-4b95-b387-f08ba97e2dd0	10km
e73d1244-73b7-4076-9bf3-93149107b6b2	4b21a4c9-a706-4b95-b387-f08ba97e2dd0	5km
9995a366-e4c2-4fe1-be9d-253c97fd3209	3f791269-6fe2-4557-83f1-740d1c44f5e3	풀(42.195km)
a6d64a1b-47df-40a1-80e3-e5f0868dfa18	3f791269-6fe2-4557-83f1-740d1c44f5e3	하프(21.0975km)
c211bdbc-62bf-4b24-b5ed-1a6271d50785	3f791269-6fe2-4557-83f1-740d1c44f5e3	10km
9bddaca2-c85a-4ef0-91c8-c2d1cb68dcd3	3f791269-6fe2-4557-83f1-740d1c44f5e3	5km
6b824197-266c-47bf-ab80-ccd101cd3315	3f791269-6fe2-4557-83f1-740d1c44f5e3	하프(21.0975km)
4f3e85b2-d04c-4875-b628-93e29df23a1e	b0658c63-86dc-4101-aeab-6863467845c6	14km
feddb4c0-37d3-44bf-8c6e-fd4d2c87b487	01186fd7-308d-40a0-a40d-e14188fdc129	13km
54716492-9ffe-4830-9db1-a7392355a5ab	01186fd7-308d-40a0-a40d-e14188fdc129	22km
c52baa0a-c852-47ea-9c93-ea73ab76d58a	01186fd7-308d-40a0-a40d-e14188fdc129	31km
7960bb01-95e2-4604-b505-7f915ba25296	01186fd7-308d-40a0-a40d-e14188fdc129	43km
07aa136d-857e-44e2-a1fe-6fe9c050906a	741c9789-64f8-464f-9e8e-bac0b367888a	하프(21.0975km)
72895988-ad0e-4767-bf59-faa8cb77604b	741c9789-64f8-464f-9e8e-bac0b367888a	10km
599213e7-5135-44d9-b334-8220b61f2293	741c9789-64f8-464f-9e8e-bac0b367888a	5km
9dfd4ad4-62e5-4818-9398-3408da051683	828b4788-0336-426e-8583-9c9a43904c18	10km
3698edb1-910f-4589-b5f8-aabc1788b655	828b4788-0336-426e-8583-9c9a43904c18	5km
1aa6d7a7-29b3-41f4-aa1c-9a71c796bcee	a49744cd-09dd-4630-836a-3f0593edf0a8	100km
d220778f-5ede-4b5d-9af5-a47ebaca453d	34e257d3-3640-4981-8fbf-66b5fb63176f	풀(42.195km)
71c476bd-a662-4c75-aadb-87cf188582d0	34e257d3-3640-4981-8fbf-66b5fb63176f	하프(21.0975km)
9be1d5fe-e7e2-4f2b-a6e0-e35002fa63ec	34e257d3-3640-4981-8fbf-66b5fb63176f	10km
86a05998-28e3-4c6a-800e-cf9f0f89afd2	34e257d3-3640-4981-8fbf-66b5fb63176f	5km
d819616a-b86c-4b19-8267-c3cbf05345f0	f54eb121-5fe6-4260-b690-ccc74513f0ba	하프(21.0975km)
53333671-9fc4-4768-810c-035282ab1c4e	f54eb121-5fe6-4260-b690-ccc74513f0ba	10km
e8dfe5ba-cff8-4d8e-9176-7a10b2d3267e	f54eb121-5fe6-4260-b690-ccc74513f0ba	5km
65a87470-7d7d-4587-91ec-0e9856e081d6	f54eb121-5fe6-4260-b690-ccc74513f0ba	5km패밀리런
d118067f-48a0-4a14-94b1-75f048fa173f	3c514ad6-1279-413a-9f03-bfcc5cb04b99	풀(42.195km)
0178b9e4-691c-48e0-b255-02721d770a8f	3c514ad6-1279-413a-9f03-bfcc5cb04b99	하프(21.0975km)
99a01ffa-8d85-4422-8705-2fa594e7be9c	3c514ad6-1279-413a-9f03-bfcc5cb04b99	10km
2ceeecb8-d083-4463-8d1d-a05f0dd25960	3c514ad6-1279-413a-9f03-bfcc5cb04b99	5km
372611a8-82c8-4eb5-aea4-5de81964e838	84bb8a86-5c9c-46cb-ab47-c7fba1f99130	하프(21.0975km)
cd41e6b2-b5e1-40b8-99e8-a348e624218d	84bb8a86-5c9c-46cb-ab47-c7fba1f99130	10km
4d9b84c4-67ca-4f73-9ee7-a401cf1809d1	84bb8a86-5c9c-46cb-ab47-c7fba1f99130	5km
67b48bc7-3e0b-4b8b-b887-180f225084b3	bd8190e1-b3d2-4bdc-be47-90d7d9c8b2e5	풀(42.195km)
22ffa362-e79d-4c39-8b60-06b7f2c6d92b	bd8190e1-b3d2-4bdc-be47-90d7d9c8b2e5	10km코스
d297bf73-9e2a-41d4-93ec-0418e2977bb2	bd8190e1-b3d2-4bdc-be47-90d7d9c8b2e5	5km건강달리기
d7992fa6-ab8b-489d-8a3b-e010c9b31b8d	bd8190e1-b3d2-4bdc-be47-90d7d9c8b2e5	10km코스
fcf83817-9eaf-4166-b85b-b31057263802	bd8190e1-b3d2-4bdc-be47-90d7d9c8b2e5	5km가족런3인
46a0847e-497f-4494-9fa7-68002be92040	bd8190e1-b3d2-4bdc-be47-90d7d9c8b2e5	5km가족런4~5인
2b11173c-6720-4a1c-879f-fd87ce7041d7	f8583c2f-5c2f-4923-8008-8e1dc2f9e8e8	10km
b0447b87-d88e-4b09-a7e8-58899e29d792	f8583c2f-5c2f-4923-8008-8e1dc2f9e8e8	5km
742ade45-b779-4383-9e24-8030ae3b420e	6f93e4c0-87c6-4249-a2aa-e1ff55eaae58	10km
874c701e-710a-4fe9-878f-bd0261f36bf9	6f93e4c0-87c6-4249-a2aa-e1ff55eaae58	5km
3f809ca1-918a-40d3-87f7-325d3167fcc4	f48a2ad0-84ae-42f0-8f11-a65575244fee	하프(21.0975km)
157f987f-e1fc-4cc7-b259-9759a8462dbd	f48a2ad0-84ae-42f0-8f11-a65575244fee	10km
9781a9c5-38d5-4f76-b8a5-24052965c2ad	1575f183-d421-4946-976d-b9359bccc99a	10km
d47888c3-4551-4459-8647-433422ebf33d	1575f183-d421-4946-976d-b9359bccc99a	5km
233e2e8f-da00-4f0c-a3c9-1ed7a40a97d1	282a6eca-5468-4ca1-8536-55194c22ed69	하프(21.0975km)
4c02f486-f7c7-4a5a-a89f-a30bb993acc9	282a6eca-5468-4ca1-8536-55194c22ed69	10km
a2544ce7-caee-4b0c-8266-0eaf6274d1aa	282a6eca-5468-4ca1-8536-55194c22ed69	5km
6a5bbbcb-9371-437f-9287-bf74a314e6b1	71369930-94be-4087-9a23-f53460308657	10km
02b31728-672e-4622-a48c-408f60c6ea32	71369930-94be-4087-9a23-f53460308657	5km
744d5f8d-ecd8-4f70-a53b-21d5a28911a0	8947549e-267b-4b3b-ad83-7dcdb6a48c82	풀(42.195km)
bf448201-6bbe-46c0-8259-32ffd39cfe40	8947549e-267b-4b3b-ad83-7dcdb6a48c82	하프(21.0975km)
ef658370-674b-4c2c-ae9f-e036e95a7e7d	8947549e-267b-4b3b-ad83-7dcdb6a48c82	10km
727ade63-1f86-4275-9b3f-6e3f309e4362	8947549e-267b-4b3b-ad83-7dcdb6a48c82	5km
8d861ef5-42c7-4c78-917d-f91773179468	8c8750cb-6a0a-43f4-a65b-3acad4f8f3a9	하프(21.0975km)
52a9c28a-0dc0-4adb-a51e-2cbffab38b74	8c8750cb-6a0a-43f4-a65b-3acad4f8f3a9	10km
6ee3c0cd-98a7-4519-83c2-4508dbbcf34b	8c8750cb-6a0a-43f4-a65b-3acad4f8f3a9	5km
cea41958-47de-4904-b011-c8c69da97e4a	9a96ae3e-95b0-4272-b658-5ee470cb1504	32km
400ebfd5-1ce1-4612-a3c1-ca67583582d8	7c8e4788-694a-4f75-ad12-3691d7ed6924	5.28km
c8fbae20-fc4c-4694-9fc3-45ed5cc4ca06	7c8e4788-694a-4f75-ad12-3691d7ed6924	10km
b472a82b-24b2-4e1c-b5d3-f8ccc1b2f8bf	bb6b43e7-8e2d-4050-a89c-b0153ecadf4b	10km
381fb8b2-5d51-44df-93be-9670f3961ac9	bb6b43e7-8e2d-4050-a89c-b0153ecadf4b	5km
3c02776b-f699-4796-97aa-1359a708eed0	cb524828-12fd-43c5-b879-7fe42b02a406	하프(21.0975km)
7f92c93c-a6b5-457e-a7ec-a058ad900061	cb524828-12fd-43c5-b879-7fe42b02a406	10km
a12215b6-7830-43d1-bd07-a7b309610573	cb524828-12fd-43c5-b879-7fe42b02a406	5km
32fda819-b2f8-437d-883b-950c4e65e7e6	6dcaff10-7eba-46f9-992d-f051a1ba5762	하프(21.0975km)
4d70cec6-b1f5-41cd-895f-97ecab6d68bb	6dcaff10-7eba-46f9-992d-f051a1ba5762	10km
4370942a-623b-4415-a98d-aee075125b45	6dcaff10-7eba-46f9-992d-f051a1ba5762	5km
e5094924-d53e-418c-bbcd-c5cfba640ab9	6dcaff10-7eba-46f9-992d-f051a1ba5762	3km
d77649f7-c997-4028-abd2-d0aacad67b19	a8723304-a1f8-4ea2-af6f-679a05861553	하프(21.0975km)
968681fb-da29-48fa-a626-acffc682721b	a8723304-a1f8-4ea2-af6f-679a05861553	10km
dd61cfbe-e4a8-43d2-9d8c-d77cd014cc72	a8723304-a1f8-4ea2-af6f-679a05861553	5km
73cbf6ae-b5c4-4fed-83e1-e5da1d9e84ed	44c45bfe-0260-4cfb-971f-500648c14728	50km.
f1cee694-3f1a-4297-8a12-59d0ffd1406b	44c45bfe-0260-4cfb-971f-500648c14728	100km
49799416-7693-4f56-b38a-3f5674b3d5fc	da8b9f0d-9ea3-4233-a664-57ef41331972	하프(21.0975km)
d179c0a0-c54b-468c-bc0f-a8aa097a127e	da8b9f0d-9ea3-4233-a664-57ef41331972	10km
f2ded39e-41c8-4e37-ba97-1299bb2e75a4	da8b9f0d-9ea3-4233-a664-57ef41331972	5km
b818d3b5-ec6e-4744-85ca-ee01e94a9be3	72e060a5-95c3-4d96-ab07-caaa32fb85cc	하프(21.0975km)
5025d99c-72b8-4106-8fe4-9c7fc6458b8b	72e060a5-95c3-4d96-ab07-caaa32fb85cc	10km
fef2331d-9bd2-4151-9c93-bc2cc7e4d0e7	72e060a5-95c3-4d96-ab07-caaa32fb85cc	5km
df197291-9d84-4c88-ab73-d1aba97781d6	72e060a5-95c3-4d96-ab07-caaa32fb85cc	3km
234dd64f-8bf2-4969-ba57-05ddcff00a65	cea0ce6b-1d18-44f0-96b2-b644e464d936	5km
4ee06ffc-ad0c-4db7-b69b-d23841946fb0	cea0ce6b-1d18-44f0-96b2-b644e464d936	10km
6162a8ab-4fb6-4840-b116-a4ee0127b98e	70ad0e67-1482-4954-904c-6e36b0f4c13d	풀(42.195km)
081e37a2-f032-466b-aa6e-0cc6472938cb	70ad0e67-1482-4954-904c-6e36b0f4c13d	하프(21.0975km)
1c990ea0-d2f3-4eca-8657-fec7988649ea	70ad0e67-1482-4954-904c-6e36b0f4c13d	10km
efb010a4-8794-42e0-8698-91bd59b5f06a	70ad0e67-1482-4954-904c-6e36b0f4c13d	5km
0fa09df5-e1d6-499a-a611-4a677208dc74	c3f2566e-c83e-445a-91a5-e87463c71bfa	하프(21.0975km)
533d295d-5567-4c49-8adf-7884fd52989b	c3f2566e-c83e-445a-91a5-e87463c71bfa	10km
237ef8ec-b0f2-4dc5-ad21-65ab77e2ef10	c3f2566e-c83e-445a-91a5-e87463c71bfa	5km
6e651b08-d6a6-45eb-84df-06f827c3e1fc	2b891150-80c9-4e50-b1d9-81b30312acee	하프(21.0975km)
32997717-ce8e-4ce2-bc7c-9922f8affef1	2b891150-80c9-4e50-b1d9-81b30312acee	10km
7f453c9a-435c-487f-89ff-6dbdfb886238	2b891150-80c9-4e50-b1d9-81b30312acee	5km
34fee960-84b9-42d9-becf-e0874dcc0f15	7e5e7a1c-49f7-4142-8bf9-0c5c0c02d309	10km
4666516f-8aab-467c-9a29-53c03385b2cd	7e5e7a1c-49f7-4142-8bf9-0c5c0c02d309	5km
c54c303d-3109-43b9-9dbd-ab73662f504f	829cf6e3-09ae-4255-b63e-f664095f3848	풀(42.195km)
81c697c2-97f7-4324-928a-3d7410e18e82	829cf6e3-09ae-4255-b63e-f664095f3848	하프(21.0975km)
15e91092-7e09-4c0a-aa27-7a243047328c	829cf6e3-09ae-4255-b63e-f664095f3848	10km
b49eba09-8217-4b5c-b2be-faae60e76d34	829cf6e3-09ae-4255-b63e-f664095f3848	5km
33276688-602c-4f8d-818d-b16543bc2cec	424f5ba2-cf0b-4d99-b379-d364fceae67d	하프(21.0975km)
ab0c8be7-2a89-4d9e-9e37-1b26cd6901e1	424f5ba2-cf0b-4d99-b379-d364fceae67d	10km
344fe66a-bf37-4a98-b811-0c8e23a4bd00	1c4f2013-6022-41ec-92d4-8b17f41f7e5b	5km
46d8f9f9-fd19-444d-a995-f4ac84f0b57e	3952afe8-f8bf-4c65-bec3-aa5244ccc749	10km
35e7b1d2-23da-432a-9f99-11b5b68be4f2	3952afe8-f8bf-4c65-bec3-aa5244ccc749	5km
72dc534f-c737-4ea6-8e57-b4fed020201a	b7d4cb72-70e6-4fc2-894c-a74788a64fba	10km
0664f92c-c6d5-4732-b499-2e6967323a17	b7d4cb72-70e6-4fc2-894c-a74788a64fba	5km
da44a44b-525c-41c0-8e56-05d32aa932f4	df12f41e-eca5-4b66-96ec-f27e3264ec18	하프(21.0975km)
629b7cd8-86aa-4324-bc32-10c9705fdfa8	df12f41e-eca5-4b66-96ec-f27e3264ec18	10km
eed3679c-eff4-4d40-8f4b-631ecad7083e	df12f41e-eca5-4b66-96ec-f27e3264ec18	5km
adc3da45-a9d8-4a9d-8310-e0a476b9e924	d5a23196-d431-4978-8d25-2d62c2551e2e	10km
5569fb72-ee73-42f8-81f6-f8706ab4f5ef	d5a23196-d431-4978-8d25-2d62c2551e2e	5km
65903ead-fc6a-4f2c-9170-f1e4eb9fb4e8	3a8f97ff-9ea5-4b4e-b957-54d46f5e44de	하프(21.0975km)
c80b58d4-882b-4d57-af8b-2c2083436f98	3a8f97ff-9ea5-4b4e-b957-54d46f5e44de	10km
bbddeb20-5858-451e-a472-51f9831f7255	3a8f97ff-9ea5-4b4e-b957-54d46f5e44de	5km
554ca149-2466-4e19-ad9e-3e94caa8499c	0f6029be-29df-418b-bf83-b516e9ad25ff	하프(21.0975km)
1a790464-c25b-431d-b630-e80cbb11f1a0	0f6029be-29df-418b-bf83-b516e9ad25ff	10km
608c93ef-7a16-4bb8-8004-ea7d1144e7f3	0f6029be-29df-418b-bf83-b516e9ad25ff	5km
e618e1d4-3ac9-429e-8874-c54b92f51338	0870a4ce-5d34-460e-a41d-d44b5f4f6964	5km
828e6b9b-b8dc-4e1b-a4f6-367512b0ee1f	0870a4ce-5d34-460e-a41d-d44b5f4f6964	10km
b5dedae9-961a-4881-a894-56b63905d398	0870a4ce-5d34-460e-a41d-d44b5f4f6964	21km
f8c81a0e-e6c4-4bf1-b7e0-cfede3922a96	1fc2fe1a-01be-4928-9898-fc0d3345a220	하프(21.0975km)
99bd7ebf-0476-45dc-bd20-92455a3d5cd3	1fc2fe1a-01be-4928-9898-fc0d3345a220	10km
80a1ecc2-e422-4131-a98c-4478818b92c3	1fc2fe1a-01be-4928-9898-fc0d3345a220	5km
c98b1300-7271-43ac-9eae-b81034f22537	679cb0d4-7dd7-4eb2-ac81-72d6ed4f7de3	하프(21.0975km)
741452f4-8ad7-40f1-adbf-cd8633959c9d	679cb0d4-7dd7-4eb2-ac81-72d6ed4f7de3	10km
4b103202-52fc-40e4-ae78-211fee6887d4	679cb0d4-7dd7-4eb2-ac81-72d6ed4f7de3	5km건강달리기
7d71c448-acc6-45b2-a12c-effa1a71262a	679cb0d4-7dd7-4eb2-ac81-72d6ed4f7de3	5km장애인부
57e31cd1-4c78-46b9-bdd5-7ca4b1a09be7	54746e65-757c-4970-99cd-d592d85877fb	풀(42.195km)
ae2065a7-0bf8-4002-8dd4-b0b898aa1fd0	54746e65-757c-4970-99cd-d592d85877fb	하프(21.0975km)
0bf0039a-1ecd-450c-957a-d399c86c0e14	54746e65-757c-4970-99cd-d592d85877fb	10km
3555b5bb-bac1-4234-9f2b-9c63474ba313	54746e65-757c-4970-99cd-d592d85877fb	5km
8e22c7eb-951b-4153-9db5-58d9abb428f5	664dc299-0766-4817-a0df-dbebc926af54	하프(21.0975km)
8ea71d82-5f39-4364-950a-8d13aeaf3ef5	664dc299-0766-4817-a0df-dbebc926af54	10km
8a60b861-bff2-4400-be3c-3465202de837	3ce29726-7ee2-4aeb-90fc-e76d8f95b703	하프(21.0975km)
1e0f624d-8c9f-4728-a5b4-8034482e3b7f	3ce29726-7ee2-4aeb-90fc-e76d8f95b703	10km
f44a3211-a45b-4d8b-b9e5-1c5cebc1f816	3ce29726-7ee2-4aeb-90fc-e76d8f95b703	5km
6f4a55d7-2f9c-4699-9c7c-864664ce8363	7640d06e-30b6-4443-96c0-e8901c20d1a6	14km
420c8cd9-fbd5-43d0-aabb-2a60d07809fd	30969e1e-7378-4f80-b372-0bb28c814258	10km
6348cb1d-8c57-4145-9e2a-a85fff828556	30969e1e-7378-4f80-b372-0bb28c814258	5km
307f9a8f-dbe6-46b7-94de-cf04840b1a2b	30969e1e-7378-4f80-b372-0bb28c814258	3km
af9c0bed-6e38-4d30-93a5-fadb4c6be5fe	552a9646-f2f2-44ed-bb0f-87eecc4926b5	5km
f7a516ab-1ef3-47f7-b87e-d132536e7459	fa8cc26e-8211-4b65-8625-f9c6d661a63a	풀(42.195km)
973fa4d0-fba5-40b3-a0f6-6e8676db5776	fa8cc26e-8211-4b65-8625-f9c6d661a63a	하프(21.0975km)
a3990a4e-e838-4fa0-a51c-2fefd8c7a160	fa8cc26e-8211-4b65-8625-f9c6d661a63a	10km
f8cfac13-5e21-4f96-a9e8-e4f93b99716e	fa8cc26e-8211-4b65-8625-f9c6d661a63a	5km
5208dbba-b808-45e8-b2ed-9f4817670339	5dd75106-a410-4247-be67-0de22b0e6ef4	42km
ef7893f3-90e6-4444-8975-a5df0dc5745f	d76053e5-8191-4f72-a133-6fdd8ffd17d8	하프(21.0975km)
665d5b42-3fc2-4525-a12e-8424d2dd221d	d76053e5-8191-4f72-a133-6fdd8ffd17d8	10km
544dc740-3c3c-454d-89ec-34f113df2c59	d76053e5-8191-4f72-a133-6fdd8ffd17d8	5km
dae10b8a-628d-49de-9cfd-0fc75cba29bb	45175919-7f76-4202-b38b-8080a27df0d3	10km
05b197cf-a996-4ff3-ad59-e971b3e5d63c	45175919-7f76-4202-b38b-8080a27df0d3	5km
8f9df27b-e77b-40c0-b627-8a33cfce793d	01b9229e-420c-4838-9ee8-e9578582822b	14km
c22d3fe9-46ec-4ab6-b4ea-93a60d749482	db2b1598-6589-4826-9cfe-d577bb347028	10km
f8e9fbcf-ea50-4c64-aff5-cea84c6f6c38	db2b1598-6589-4826-9cfe-d577bb347028	5km
fc461e8d-89e3-4686-b041-a376e88eb63b	cd08e775-6dbf-4f94-aa03-b878f5acfebc	하프(21.0975km)
f383a3d7-2059-4eb8-a007-e7b17a5600c2	cd08e775-6dbf-4f94-aa03-b878f5acfebc	10km
329944dd-3dc9-4c11-9b38-2e4ceaa8b63f	cd08e775-6dbf-4f94-aa03-b878f5acfebc	5km
b0914a17-c014-4eb9-aa29-f87ca8749977	cd08e775-6dbf-4f94-aa03-b878f5acfebc	5km걷기
e9e601b6-1d09-4bea-bb1f-4313c19cc008	e60f5504-de56-4717-a72e-ff596b5104ae	5km
1e97f4e3-48e9-405a-a89a-8d5748c6025f	dd28d251-aea1-440b-b715-db0cc0eac51a	10km
3e4919e4-cfc2-4630-a9a2-b9ed2e151a46	dd28d251-aea1-440b-b715-db0cc0eac51a	7km
2ac41790-0d9c-4450-bd43-704dd9283aac	dd28d251-aea1-440b-b715-db0cc0eac51a	5km
315a89cc-5e78-4680-b684-6e36bcdeb1cc	78a95536-5f5f-476a-94c5-1058185ae73a	10km
6e476ed6-1120-4930-89a0-f7a91b822026	78a95536-5f5f-476a-94c5-1058185ae73a	5km
5c371cd1-317c-4cc9-bfb7-af96a1a4a046	af4547de-5912-47d2-aff6-b8023c596f88	하프(21.0975km)
93b0c947-739b-4010-9117-d54aaee292d1	af4547de-5912-47d2-aff6-b8023c596f88	10km코스
5705391a-4aad-4a8e-b61f-5945a7700a32	af4547de-5912-47d2-aff6-b8023c596f88	5km코스
9966b7cd-e1b7-4aea-8ee1-d38bd58bff62	af4547de-5912-47d2-aff6-b8023c596f88	5km커플런2인
406f1e8b-2ceb-4329-b589-1b0efd7a50fd	af4547de-5912-47d2-aff6-b8023c596f88	5km가족런3~5인
c184552f-2609-4f9a-b235-0e30b9df58e4	1662767b-b1f7-46a9-9429-5c0dd2c0e22b	10km
aed993fd-4b9e-4aa4-9d62-771269fee822	1662767b-b1f7-46a9-9429-5c0dd2c0e22b	5km
760532c7-4c51-4f98-ab71-d142894142b4	ac792086-5090-402f-af03-e26cf8431a26	10km
6d0839c8-4644-4017-957e-763d81f1bdf1	ac792086-5090-402f-af03-e26cf8431a26	5km
05f40511-a4c2-4b01-b8a5-b25efee973c3	54e95088-9864-40bf-8c7a-0fa86f0ee9ba	51.8km
291658f5-4080-4392-a2a7-028e34f7885c	54e95088-9864-40bf-8c7a-0fa86f0ee9ba	43km
824b4201-cca0-4699-92e0-73122a8e2ca0	54e95088-9864-40bf-8c7a-0fa86f0ee9ba	33km
27b5df37-ae24-478d-90a1-45b9e7bcc34c	54e95088-9864-40bf-8c7a-0fa86f0ee9ba	23km
72c29d01-69a6-4262-a96d-493069f20d12	54e95088-9864-40bf-8c7a-0fa86f0ee9ba	13km
c54316ab-c720-4677-a5c3-c3d3bea4f8b5	54e95088-9864-40bf-8c7a-0fa86f0ee9ba	5.18km
04927dd6-483d-4bb1-b367-69aff3935ba1	decc0283-05ce-402f-a5df-e1c75f520c8d	100km
6088e02a-dccc-472a-af25-d912bda0aa47	decc0283-05ce-402f-a5df-e1c75f520c8d	50km
aee45fb8-4a09-4521-bbb0-893b21f01de8	b1739738-0d9c-486a-97dd-0ea6ebe77639	10km
fa359f02-59ad-4fe4-9284-a99800b39276	b1739738-0d9c-486a-97dd-0ea6ebe77639	5km
f426f017-701d-4304-b0ab-b4457180870b	6225d03e-1d13-4e6f-bdeb-8e9ecd7b0ac1	하프(21.0975km)
2ffc3191-3784-462f-ba6c-e78723b552d5	6225d03e-1d13-4e6f-bdeb-8e9ecd7b0ac1	10km
673e5ca4-52ee-49db-a55a-8b6c7fe9efcf	6225d03e-1d13-4e6f-bdeb-8e9ecd7b0ac1	5km
7da053cc-9872-4d56-93b8-82be742658d1	6225d03e-1d13-4e6f-bdeb-8e9ecd7b0ac1	3.5km
3755439a-6c70-4a66-b0e7-e26c51d58a8b	438f68fd-d85b-432c-9e6b-75925f9b01f9	10km
09605e11-aea2-44aa-81d4-8a11b4df5a51	438f68fd-d85b-432c-9e6b-75925f9b01f9	5km
cd91d352-2c19-4d46-89f8-7080083e2dbf	53eb9e41-2371-4d55-bb47-1f0d5de13c7e	풀(42.195km)
c063602a-d981-4b26-9f4c-84f18f262032	53eb9e41-2371-4d55-bb47-1f0d5de13c7e	하프(21.0975km)
f3ce7331-8b92-42da-8ac6-fc727eda591f	53eb9e41-2371-4d55-bb47-1f0d5de13c7e	10km
b217c96d-fe40-4e4f-840d-fb4b2ceeb54b	53eb9e41-2371-4d55-bb47-1f0d5de13c7e	5km
6054e454-1028-4cb1-953e-2c4581629ed6	dfdc5b32-a5f2-4267-aa9e-b35b64dfa99c	10km
59c504c4-7b33-44e8-9bfb-469e785cb328	dfdc5b32-a5f2-4267-aa9e-b35b64dfa99c	5km
7c6dcc3f-3463-4b84-b2f5-f54bd38a7627	dfdc5b32-a5f2-4267-aa9e-b35b64dfa99c	3km걷기
4c6409d7-b13f-4b34-bb44-c304f29c6757	2068190e-99bc-4222-8482-cc19c5670330	하프(21.0975km)
4d88aa01-a947-4c3a-90b8-e159cfa3cd73	2068190e-99bc-4222-8482-cc19c5670330	10km코스
9a7de691-6510-4a18-8a3e-b16e2b7cfa3e	2068190e-99bc-4222-8482-cc19c5670330	5km코스
ba7ebe56-84bf-4c0d-9b0c-2b300e5c86f3	2068190e-99bc-4222-8482-cc19c5670330	10km커플
f883f103-4c02-41f8-ba80-8a20819fc01b	2068190e-99bc-4222-8482-cc19c5670330	5km가족3인
954a0393-a041-45f1-8f2d-4e7152d509b5	2068190e-99bc-4222-8482-cc19c5670330	5km가족4~5인
93af19e4-99c5-416f-9623-e1bacd61b89f	9937b561-a80c-4e8c-b323-0e761cb25fe9	10km
0300b8bc-b577-43e0-b06a-30a75dfe035b	9937b561-a80c-4e8c-b323-0e761cb25fe9	5km
9b70abd0-e5a0-4740-b874-e74a898bee38	09d000ce-4a14-4dc9-8657-58199be8264d	하프(21.0975km)
0071c963-1571-43b6-8f74-d14da0248fc1	09d000ce-4a14-4dc9-8657-58199be8264d	10km
787fbab4-feda-4231-a145-1bdbcd23d983	09d000ce-4a14-4dc9-8657-58199be8264d	5km
02481c68-f2dc-4425-ab5d-c4455698b8c6	86c6837b-49a8-4e87-b092-ec32c7564bbd	100km
bdc620ec-4958-4dcd-b5e5-50af623d6c5c	86c6837b-49a8-4e87-b092-ec32c7564bbd	50km
f258d794-dc01-4d66-ae99-8f9d62d3c590	8697f96a-347c-4c19-b7aa-e09093edabc8	5km
85e7d4b8-615b-47d7-8207-f91f54c6e50b	dfca79f7-27ca-479e-804b-0c8c895f3065	하프(21.0975km)
95116415-7c26-4fd8-b87a-a2b8e0edd4b9	dfca79f7-27ca-479e-804b-0c8c895f3065	10km
d0c83e22-4953-4a79-98e6-b2d710405991	dfca79f7-27ca-479e-804b-0c8c895f3065	5km
b80baa6c-4d2e-4d32-9b55-adc17033bd56	129b9441-f4c9-4457-8656-223c002843e2	25km
e4adb76b-6816-400d-8542-a88bec16738a	129b9441-f4c9-4457-8656-223c002843e2	12km
ca285e26-e8a5-425f-9f36-1db2275becef	62aa9950-b270-4c9f-b995-d5cb1c91f193	하프(21.0975km)
6af149b1-f928-408a-baac-c9b886c0b140	62aa9950-b270-4c9f-b995-d5cb1c91f193	10km
49ff3c47-7a8a-46e6-98b4-696a554094d8	62aa9950-b270-4c9f-b995-d5cb1c91f193	5km
81716f2d-b4e6-4184-aa76-a4c1c4b603d5	2c23bbe7-12da-417c-bc20-3d2ff9dd2a78	일반부5km
f559c98e-7995-4679-a3b9-caa1f323611d	2c23bbe7-12da-417c-bc20-3d2ff9dd2a78	초등부3km
4824b6d9-bb22-4c06-bd43-7bbdfaf283d8	2c23bbe7-12da-417c-bc20-3d2ff9dd2a78	1.5km
b223620b-a366-45aa-9731-021c5d334146	7c0e4cc9-7eb2-4389-9532-ba0389c854e7	하프(21.0975km)
2145651b-a5db-40d6-a573-ed6481b9730f	7c0e4cc9-7eb2-4389-9532-ba0389c854e7	10km
d80c25f5-d211-4f91-b3f6-eb87dae61bb4	7c0e4cc9-7eb2-4389-9532-ba0389c854e7	5km
a5e8a6c3-eb8a-43be-bbd3-a1e6e7c21a56	49e11052-b352-47af-bb37-55bab98d7146	3km
46764312-fc22-47da-8336-f39aed8ea878	9b8f40d4-7e04-4521-a3cb-32070a2a7a55	하프(21.0975km)
d89e9653-4e0e-466e-98c8-5f1fd55390af	9b8f40d4-7e04-4521-a3cb-32070a2a7a55	10km
613bed54-7d67-4a7c-a95c-57a0cc34ec28	9b8f40d4-7e04-4521-a3cb-32070a2a7a55	5km
6847719b-b3d3-4a92-b7bd-85c378c3256b	e531c8ff-79e7-42d7-9f4d-eaf7d14678f5	10km
00fba589-4578-4174-9e04-7a3b8f01c167	e531c8ff-79e7-42d7-9f4d-eaf7d14678f5	5.18km
37e1adbb-5636-4ff3-a9fe-c84ff103c6c3	1a6b7b21-d5f8-4d6b-8e67-971661eaa3c4	100km
f30d89a1-7054-4558-8c40-13563e253fd6	1a6b7b21-d5f8-4d6b-8e67-971661eaa3c4	50km
8bbce520-04ad-4b4a-9e50-8eea3b28fd30	1a6b7b21-d5f8-4d6b-8e67-971661eaa3c4	25km
87c7837a-8e8d-4585-83cd-ab3b36035bb2	150ba389-9d17-49d9-92d0-78574e257f0f	10km
17023799-e719-449c-8355-10ea0c59e4a4	150ba389-9d17-49d9-92d0-78574e257f0f	5km
b07ecb22-1580-418a-8d20-b9b30698a89d	3364bc65-6806-4ebd-b1f9-0149f61b8c2f	하프(21.0975km)
7f885b44-3cc4-43c7-84ce-24191c8056d5	3364bc65-6806-4ebd-b1f9-0149f61b8c2f	10km
d9c7fb09-ecee-493f-ba22-d14001337605	3364bc65-6806-4ebd-b1f9-0149f61b8c2f	5km
60af3143-7f11-4dcb-92e5-0da6283dd1bd	3929b66d-e1d1-4af7-8994-9edad8065207	10km
4e5b6bf2-0c5f-4e49-91b0-a058cda4f5e1	3929b66d-e1d1-4af7-8994-9edad8065207	5km
230b994e-eb21-4a4f-ae09-ce9e7a74364b	77191212-0d8f-4277-8406-d1abb6820970	10km
e6837ac1-332e-48b9-a2bc-ee3ddc4f217d	77191212-0d8f-4277-8406-d1abb6820970	5km
e3559281-51b3-4637-996a-a632d341a140	762e9377-607f-4da9-a536-a553dc281314	하프(21.0975km)
29bf7973-b817-4c67-b459-6bdee7474fbc	762e9377-607f-4da9-a536-a553dc281314	10km
3686ad39-bfac-44b3-9652-699ffbf32d5c	762e9377-607f-4da9-a536-a553dc281314	5km건강달리기
bc12083d-98a1-4066-8df9-337b2b27957b	238df25d-47a1-4b8e-b772-39b8f8a3f05f	풀(42.195km)
6dc419ec-cc48-46ab-ad69-1890033e7ac3	238df25d-47a1-4b8e-b772-39b8f8a3f05f	하프(21.0975km)
73d80888-719a-4934-98f2-dd36478f8253	238df25d-47a1-4b8e-b772-39b8f8a3f05f	10km
b2149a08-d929-43dc-bf37-084c182bb1a7	ccd33944-f768-424b-908b-e1b083024197	21km
1ad768f2-6681-4013-a70c-1e24ecca672e	ccd33944-f768-424b-908b-e1b083024197	10km
e810fbab-da6b-46ca-a435-b5ef23e6d25e	ccd33944-f768-424b-908b-e1b083024197	6km
3fff976c-9ee8-4333-af84-bc5949bccddf	194de71f-fb52-4f26-b90c-ffd3e3bd2851	하프(21.0975km)
2ae23497-184f-402c-87de-0be44159a590	194de71f-fb52-4f26-b90c-ffd3e3bd2851	10km
9cfc3650-a96c-4d38-932a-be66b7b805a2	194de71f-fb52-4f26-b90c-ffd3e3bd2851	5km
372a3712-5838-43f7-98c6-be851e705b3c	25be7dce-d6ff-449c-9dcd-fd5dd5314034	10km
7baf8879-e421-4255-90a8-fbeec804148a	25be7dce-d6ff-449c-9dcd-fd5dd5314034	28km
8ce7a69e-6841-4de0-b544-73d0f9af0328	770a2ec0-b7bb-440f-b706-52cb4b135d3f	48km
1b5b4a1c-3c2d-4e7e-9140-97eb72962b73	770a2ec0-b7bb-440f-b706-52cb4b135d3f	40km
89474e48-202e-48d0-ae16-fb2a79b43d8d	770a2ec0-b7bb-440f-b706-52cb4b135d3f	34km
5e8ba720-6ed1-4f30-8454-7ce5057fbde1	770a2ec0-b7bb-440f-b706-52cb4b135d3f	21km
69f14b05-8107-489b-b853-7344d681cf3a	642e7120-ed24-43e2-aee1-d9e5746e5e46	10km
bba143df-9246-4ab7-bcc6-50cbb7a80759	4a86b226-b645-4bf6-badb-6ffb1f71bbe6	10km
e7513eec-9b0c-4ddd-86f3-e7e844747efd	4a86b226-b645-4bf6-badb-6ffb1f71bbe6	5km
26b843f6-ce77-49e0-8a3a-2b195b784039	eff919b0-5ca6-4355-be21-466330ec7683	10km
49112887-9a38-4d67-827f-1d3585a9aac0	eff919b0-5ca6-4355-be21-466330ec7683	5km
e292d0aa-ef89-46dc-a7af-3c861fca095d	e264e395-a6bc-4777-85f3-15215b03a90b	10km
415a2d8b-751f-40ed-b564-6d764ffd5324	e264e395-a6bc-4777-85f3-15215b03a90b	5.31km
35afb9e3-51ef-49fc-b8f7-60342a49a8cc	ed6f9b8c-c7ed-4e70-a917-c42d4e77b931	10km
7ecc59e2-0e72-4163-bf8c-c3bb6dfbf8a7	ed6f9b8c-c7ed-4e70-a917-c42d4e77b931	5km
d2dcdfd3-58cd-437b-bfb3-3fa49b8bb477	4828a81f-5334-4465-b32f-a815528ad7e2	10km
d2e46685-dcb6-4702-8927-aec8c47f70e5	4828a81f-5334-4465-b32f-a815528ad7e2	5km
9788ea44-c5c2-4d3c-bf91-efe2febd2019	a5da1a1d-0e81-49a8-9265-9b33877b202a	하프(21.0975km)
b0b91cf3-5b74-47ea-8de2-196cdd58e1ed	a5da1a1d-0e81-49a8-9265-9b33877b202a	10km
2cf9bf79-c581-41c8-98b2-427f9ed5287f	a5da1a1d-0e81-49a8-9265-9b33877b202a	5km
0f70b009-0ce9-4c61-9d6e-6eaa74e23467	a5da1a1d-0e81-49a8-9265-9b33877b202a	3km가족런
80186593-14c0-42b7-939f-da8315dc18ff	41c312bc-c0c3-4d6f-9d13-4f6f5d7d0230	8km
797f77b5-92d1-4dec-bf1d-6a7fc0577526	41c312bc-c0c3-4d6f-9d13-4f6f5d7d0230	16km
083559a9-c8e2-47a6-9dae-09acb5fe14fe	41c312bc-c0c3-4d6f-9d13-4f6f5d7d0230	24km
09c0f438-43dd-4ad7-948d-888c5ba035d2	6c441c46-c257-4bc1-b045-41d22b324c71	10km
f4fd1da8-eb06-445a-8e92-a40c95cfaa1e	be254efb-6169-4a71-9277-f247d3f9b3ba	100km
08e6bbb8-385b-436a-b0fc-9c5294d885b0	be254efb-6169-4a71-9277-f247d3f9b3ba	200km
a67a540c-26c8-4abd-a017-20946f46684b	92cb209a-7ac2-427b-bf55-ea64da9e9ad7	하프(21.0975km)
b75f3cd5-efd5-477d-b0f0-26590a389569	92cb209a-7ac2-427b-bf55-ea64da9e9ad7	10km
dcbf9003-3aeb-465e-91f6-64fe96438b55	92cb209a-7ac2-427b-bf55-ea64da9e9ad7	5km
649afd5a-f10d-4884-a1b1-257f08783fcc	26daa80e-af0e-4e35-9c37-779e77ae5e63	10km
bf4991ae-8be3-4f9d-88ff-34e76607118d	26daa80e-af0e-4e35-9c37-779e77ae5e63	5km
79d68221-6bb2-4d8f-b1c0-cfc7b01cb6a1	c6e821bc-ff40-4e54-af55-feea12ae3f3e	25km
333e1001-2585-46fc-8665-8191a2df20f6	c6e821bc-ff40-4e54-af55-feea12ae3f3e	14km
87ce6041-ea3c-485f-a1be-6c03ae3f46a2	12f28e97-767a-47ef-b1dc-26d876e85e59	하프(21.0975km)
a442225a-e570-41cd-a4c7-9dd735086c0f	12f28e97-767a-47ef-b1dc-26d876e85e59	10km
92efb461-fed2-43f9-9efc-1c6b2f052392	12f28e97-767a-47ef-b1dc-26d876e85e59	5km
e04e7db8-589b-40d6-88f8-67774a87552c	3f16fb15-3c1e-472e-9b10-042525458a7b	하프(21.0975km)
ee813340-2476-4f06-bf25-b767697aa033	3f16fb15-3c1e-472e-9b10-042525458a7b	10km코스
3cdb4cbb-f128-46ff-b723-504617f585ac	3f16fb15-3c1e-472e-9b10-042525458a7b	4km코스
08cab48c-c1ac-4f18-be38-b8a385651bc0	3f16fb15-3c1e-472e-9b10-042525458a7b	10km코스
accea01b-3e3f-4a77-aea0-d2a6d02d7f3e	3f16fb15-3c1e-472e-9b10-042525458a7b	4km코스
157da385-b757-44d2-8c49-4fb92bf90649	36e7f433-0c88-4d19-ba65-77e287f4ead5	10km
6b3d695b-6b51-4786-8393-7df098feab5b	36e7f433-0c88-4d19-ba65-77e287f4ead5	5km
1179afa6-9b96-4667-85c6-990308bb250d	6707e8fe-efe3-40c5-ba47-c5ad938e9d0e	10km
56126272-a9b7-4e70-a467-f04abeea0179	6707e8fe-efe3-40c5-ba47-c5ad938e9d0e	32km
574731d7-99df-4ebb-a691-6b12fcdf4b81	34b07196-0420-4c90-b78f-761daa4703c1	20km
587310c6-7ddb-4c1d-8b81-d33a9ec49a0b	34b07196-0420-4c90-b78f-761daa4703c1	12km
a5fc6925-174e-48ae-a439-e7d583546db9	ae5e60c4-c741-440f-af63-3c6e77c77276	풀(42.195km)
3a4a1596-801b-48d8-99f1-e8e7337c4d97	ae5e60c4-c741-440f-af63-3c6e77c77276	하프(21.0975km)
11adc103-9852-4f35-9ac5-c600b46681fb	ae5e60c4-c741-440f-af63-3c6e77c77276	10km
54758757-939b-48c5-9da7-7fa2d0ec1ba1	ae5e60c4-c741-440f-af63-3c6e77c77276	5km
92213d80-1dc5-4bd6-8e77-706559b58f07	f832c38b-71cf-4b4a-b08c-6579a8bd56cc	하프(21.0975km)
e2749311-a042-42d9-96bf-1b5fff44915a	f832c38b-71cf-4b4a-b08c-6579a8bd56cc	10km
08069f58-7ea4-40e5-8bdc-14b8a96fb293	f832c38b-71cf-4b4a-b08c-6579a8bd56cc	5km
0e406645-a5de-4997-8426-3f097e3831bf	db95b70d-1e65-4740-97c2-f64cc19097e9	100km
5aebd276-2fc3-4d1a-85d2-e44c3626ef25	db95b70d-1e65-4740-97c2-f64cc19097e9	50km
82f04e18-43f3-4cfe-ae31-cbdd6885c125	3bad951a-53c0-4cf9-b8ab-cf28d6618e94	하프(21.0975km)
232a32f4-072f-4732-9d29-a1f10b198ef2	3bad951a-53c0-4cf9-b8ab-cf28d6618e94	10km코스
c5dbebd8-2e2e-464c-b300-c29e6db8712c	3bad951a-53c0-4cf9-b8ab-cf28d6618e94	10km커플런
32d725f0-c157-4173-b449-2d678196779a	3bad951a-53c0-4cf9-b8ab-cf28d6618e94	5km
3f728b56-870c-4d5f-a628-086aa8415d17	b1886f12-9e4f-4e59-97e7-30272acf014b	하프(21.0975km)
084e4ce4-d3e9-42ab-81f1-402124d8e00b	b1886f12-9e4f-4e59-97e7-30272acf014b	10km
e28ae9c8-a907-4cee-9f2a-5df085365fae	b1886f12-9e4f-4e59-97e7-30272acf014b	5km
88f28ecb-9017-4514-96dd-208f24528e4b	3b2dc722-9a6d-4e5d-9e91-c9c21ab51594	5km
54e7c1f9-ea95-4ba2-9c96-1b920461a863	3b2dc722-9a6d-4e5d-9e91-c9c21ab51594	10km
59795574-e119-4693-a79f-2fc28f720363	5005dee1-27de-41e6-a31e-f23675a6e409	하프(21.0975km)
8a0ba908-e32a-42fb-a92e-bb139497cded	5005dee1-27de-41e6-a31e-f23675a6e409	10km
289fb2b2-0cdc-4ec0-a7e2-f207a620271d	5005dee1-27de-41e6-a31e-f23675a6e409	5km
489a2d23-a0e4-44e5-ab88-0bc7e7d6fd99	88279487-1e8b-4ace-baf2-0fcf5f34b865	10km
3ae97445-4d63-4640-9541-118cbca39eee	88279487-1e8b-4ace-baf2-0fcf5f34b865	5km
a5b520b0-c527-4479-a0cc-da57af60eb55	1648b380-d11f-4ede-ba38-d20f702474e6	하프(21.0975km)
17fcff29-3b65-48e9-8927-e268f4055d16	1648b380-d11f-4ede-ba38-d20f702474e6	10km
f60b783e-c051-4210-aa96-56aed9439911	1648b380-d11f-4ede-ba38-d20f702474e6	5km
497cbd0c-fe20-4b06-bf0d-b2e3b9564bbf	8cea705c-e1b9-440b-9a57-4ed2821a5041	하프(21.0975km)
54e0e472-7da2-4e34-a5b0-051027511993	8cea705c-e1b9-440b-9a57-4ed2821a5041	10km
b73539b9-fa3c-4a5c-b1d2-ab55539a771d	8cea705c-e1b9-440b-9a57-4ed2821a5041	5km
18866bfc-286f-40a9-80c3-565f8bc6e585	c12b5959-e634-49ef-b23c-4a7bcfda0b13	15km
69e8149f-41e3-4e9f-8000-a361cb2bcfbe	c12b5959-e634-49ef-b23c-4a7bcfda0b13	10km
0dd25853-cb36-4462-8d07-0b8046f769aa	72daa762-e73e-4f70-958d-7852766c11bb	10km
2c5867f1-e4b1-4918-949c-5b78f7257d34	72daa762-e73e-4f70-958d-7852766c11bb	6km
8b8f38e0-d3ff-49f8-8edd-151acd247bce	2c8e67ef-93c6-42a0-9f62-375bb03d073d	100km
a88c144e-9e7c-4837-8656-e7b51c18da10	2c8e67ef-93c6-42a0-9f62-375bb03d073d	50km
39bf6790-e5c1-42e6-9526-3967b287fe5a	ef1a1671-858d-4586-b92f-67898efe2cca	11.8km
43576c8a-b4f2-469f-8e3c-4aaeee37bdcc	e9ccb0d1-abc1-47bc-a54c-1cb632fa5ded	10km
e904130f-5e49-4823-862f-a6b9bbf49997	e9ccb0d1-abc1-47bc-a54c-1cb632fa5ded	5km
5cf9673d-23f3-410c-ac06-b78276380602	281d344f-7009-4a63-8c60-f0961d1171b8	10km
5e2a9b40-3702-415c-8ff0-4ca289c42ede	fa61eeda-6ea4-431c-be9a-0b90d35dbb89	13km
5fd4b8fc-d027-47dc-91f7-0f6a146c9883	1e46bd10-2f9a-423a-946e-06d61ab1820c	10km
f48e6a3f-95a7-4e89-92c6-aa0207082408	1e46bd10-2f9a-423a-946e-06d61ab1820c	5km
cbbb5317-5fc0-43b6-8dfc-2391bdd38bfb	1e46bd10-2f9a-423a-946e-06d61ab1820c	5km가족부2인1조
17de02b4-9629-452c-beec-8f644ee83649	1e46bd10-2f9a-423a-946e-06d61ab1820c	10km단체전3인1조
7141745d-6d5c-460a-b577-b4c8117a09d6	13687f3a-a3ef-4588-a871-d8ec83ee28a6	10km
13c3d01b-030f-40cd-a4fb-56f702766996	13687f3a-a3ef-4588-a871-d8ec83ee28a6	5km
bf3e7c1f-5605-4a27-8e18-a7653b878d68	9f78aa16-c445-48c2-9047-30db8df71cad	하프(21.0975km)
8ffb15e1-3762-4906-bf86-79196df67a24	9f78aa16-c445-48c2-9047-30db8df71cad	10km
6722c278-3850-4af1-9d8b-cfcf2e00126b	9f78aa16-c445-48c2-9047-30db8df71cad	5km
c2357466-f746-4922-b24c-b95c65e40ab6	7efa7b1c-3878-4b0b-8e67-6d4e4ee36d72	하프(21.0975km)
2f880dc4-a12a-4371-ae37-fd963f4bc70e	7efa7b1c-3878-4b0b-8e67-6d4e4ee36d72	10km
83f25349-f4fb-47be-8de4-e905317d58dc	7efa7b1c-3878-4b0b-8e67-6d4e4ee36d72	5km
88352d0a-44bf-4047-9d98-786b75d2eeeb	ac42ff2e-1dd0-4955-9321-74f4be8dff93	40km
3f28d111-0a2c-494b-a487-dbaecd942c16	ac629528-0f47-487c-8c7f-fea9e4801d9e	21km
dbefe64a-9d04-4a98-98fc-88d94e8c7791	ac629528-0f47-487c-8c7f-fea9e4801d9e	14km
c8995d74-2e00-4039-add6-9e8a94a2fa34	ac629528-0f47-487c-8c7f-fea9e4801d9e	7km
c6f4888c-994d-4eb9-8299-75c976bc3f4a	3b9d4c87-b897-4346-b5c1-e7f87476cf20	10km
a270ee1a-3616-44c4-8d99-13f17e883150	26014c6c-5454-4c22-9469-c95da5dbf5d4	22km
53ba3687-9ab2-4276-8362-dacfa9ba18be	26014c6c-5454-4c22-9469-c95da5dbf5d4	13km
d8ebc0ff-2257-4c1e-9f82-810195aa3119	50b25e0e-4a08-4512-9da8-c40e5d427418	5km
f5c1f661-76d1-4fd7-9dc1-8b9774f5792b	50b25e0e-4a08-4512-9da8-c40e5d427418	10km
e0fa1260-5ccd-4657-8bce-8795560819de	50b25e0e-4a08-4512-9da8-c40e5d427418	15km
740f6ea1-a943-452c-8e83-49fbe6097937	a716e4cb-bf3b-4455-8eab-4c12b83c58e0	하프(21.0975km)
089edefe-e64e-4a90-874a-9920dffc8af5	a716e4cb-bf3b-4455-8eab-4c12b83c58e0	10km
cd79bfa7-8ddf-4e7e-a929-0bb156cc3d78	a716e4cb-bf3b-4455-8eab-4c12b83c58e0	5km
0f69a556-1355-438c-aa2e-528a296e5e87	7a0bf3bd-ea58-4c7a-81d1-7329b8acc9a0	102km
5948f1d7-d9c7-472e-a564-ff5240053138	6b0c44ba-c4d0-4783-a475-106c030908d1	하프(21.0975km)
d0b0499c-bce2-485f-8a9e-b8c4f0720c36	6b0c44ba-c4d0-4783-a475-106c030908d1	10km
7490c05f-1320-4057-b17b-8f388243f55e	6b0c44ba-c4d0-4783-a475-106c030908d1	5km
2b43e77d-f880-44c1-955a-954412014549	1ecb1f65-9e48-4e64-95d0-83509a47a051	풀(42.195km)
c6015c70-0af0-404e-956c-de813b15d4a5	1ecb1f65-9e48-4e64-95d0-83509a47a051	하프(21.0975km)
eead8a6d-771b-419e-98d0-ef1ad9573a83	1ecb1f65-9e48-4e64-95d0-83509a47a051	10km
2f665c68-ed90-4f27-9db9-6d444f10c287	1ecb1f65-9e48-4e64-95d0-83509a47a051	5km
f7d0ba22-0a80-4e16-8098-a2d4cdb07e56	4ec8e96d-1fb0-4168-afd2-d0aed696c07e	하프(21.0975km)
1f1e1941-317b-46aa-b1d2-6b0ddd0b253b	4ec8e96d-1fb0-4168-afd2-d0aed696c07e	10km
9f95139a-bf82-4640-91ac-6acf2279a18e	4ec8e96d-1fb0-4168-afd2-d0aed696c07e	6km건강코스
8d0748ab-db8a-4534-bb3d-8d1ff5e0b41f	4ec8e96d-1fb0-4168-afd2-d0aed696c07e	6km패밀리런
e653eff1-039f-4bdd-9a87-74944880d30f	0e6fc03e-97ce-48b8-a4b9-0eb943671d00	풀(42.195km)
ffe788be-d607-4894-8475-1e43b5b10381	0e6fc03e-97ce-48b8-a4b9-0eb943671d00	하프(21.0975km)
cfc2b0c1-5026-4bf5-965b-eca7bb22fe5c	0e6fc03e-97ce-48b8-a4b9-0eb943671d00	10km
2b865cb0-7c16-41b4-877c-94dd3ac31a6b	0e6fc03e-97ce-48b8-a4b9-0eb943671d00	5.5km
e8f62788-4806-40fc-a8b5-f84e6c0c1f6d	a2ca0905-9a8a-4a3e-a071-6b80fb85e254	하프(21.0975km)
cff8c8d4-d6d0-43bd-9b57-c1eb45b611ca	a2ca0905-9a8a-4a3e-a071-6b80fb85e254	10km
e6026acd-9c5d-4a33-9862-839462aa9783	a2ca0905-9a8a-4a3e-a071-6b80fb85e254	5km
9c891633-4bbb-4ab5-a79d-2fd5c11894b9	e5c89de7-de95-435b-8444-a76d9c9c969f	하프(21.0975km)
228a29f0-343c-4a6f-b3b3-135d81fdc7e8	e5c89de7-de95-435b-8444-a76d9c9c969f	10km
271d8971-2f5b-468b-972e-0b26289e7434	c9f72809-15c9-4166-a18b-ebf4ee63537a	100km
8f329fc3-c8fb-4b8b-9feb-39fa796f1950	c9f72809-15c9-4166-a18b-ebf4ee63537a	60km
999208f2-38da-4ce2-a697-cffb15c21343	8ecf9efc-9562-4081-be15-a169e4d34d5c	풀(42.195km)
adfa02bc-5015-4111-b465-f96755045889	8ecf9efc-9562-4081-be15-a169e4d34d5c	하프(21.0975km)
d044157c-4027-42f4-ac13-7126cc606413	8ecf9efc-9562-4081-be15-a169e4d34d5c	10km
8c9e5ccc-ed16-4a1d-b76c-eea33c7abf06	8ecf9efc-9562-4081-be15-a169e4d34d5c	5km
e7c2d760-72e0-4555-b747-7cfffef9e21d	afd9979c-c3b9-47d0-bf2e-fb4ec21108ab	하프(21.0975km)
ead160fc-7c33-40e4-a95b-44c1e920d2d4	afd9979c-c3b9-47d0-bf2e-fb4ec21108ab	10km
2ee5a7fe-bbdc-44ad-bb41-28c6169d9aa8	afd9979c-c3b9-47d0-bf2e-fb4ec21108ab	5km
602c2d84-901e-47bb-bb59-76b3e4ffd857	5081e2f1-ebb4-45e8-a9be-971524b92fb7	하프(21.0975km)
06386177-02a2-44d3-ad3b-11f603d224de	5081e2f1-ebb4-45e8-a9be-971524b92fb7	10km
d63132b6-d729-4abf-baa0-f5e83b0a3488	5081e2f1-ebb4-45e8-a9be-971524b92fb7	10km커플
995d71d9-33bf-4dc2-a5cd-102c2deb61bf	5081e2f1-ebb4-45e8-a9be-971524b92fb7	4.2km
81c9fd65-3ebd-45ce-85b7-5dc5302b10ce	5081e2f1-ebb4-45e8-a9be-971524b92fb7	4.2km가족런
80938ef3-a012-40ed-9dc3-ade24d2e628b	6f74f786-cb40-47c5-83c2-d15ca684a799	하프(21.0975km)
dbbe1b8c-68cb-48e6-acfe-2c2c71029a56	6f74f786-cb40-47c5-83c2-d15ca684a799	10km
b434ccd9-a783-45f6-8901-24a6efaa3493	6f74f786-cb40-47c5-83c2-d15ca684a799	5km
5addfe77-3ef0-421c-a335-811573488438	cb902274-4e25-430f-b1d0-247277527321	하프(21.0975km)
485285ad-bf41-4d0e-a8fa-d76b6e999e62	cb902274-4e25-430f-b1d0-247277527321	10km
9f39a8ad-f142-4f85-ae34-7611e04e2d3f	cb902274-4e25-430f-b1d0-247277527321	5km
fae11090-eb33-4e5e-98db-4d03f6d659c4	6e9041d0-5685-49cf-8d30-64ec4a812b59	109km
73f1adea-9c94-49fe-a011-9a7a3acb9b4e	6e9041d0-5685-49cf-8d30-64ec4a812b59	50km
8a97c951-0d8a-4522-8a0c-2b61fbba2117	6e9041d0-5685-49cf-8d30-64ec4a812b59	30km
86b2f7a5-fe4e-4be2-8e87-a17a894f3b1b	6e9041d0-5685-49cf-8d30-64ec4a812b59	20km
dfb6257a-4e01-4d8a-8f78-652f4e87dee4	6e9041d0-5685-49cf-8d30-64ec4a812b59	15km
52c75a18-d981-4ce5-82f7-96ee164e7a69	99158f03-1565-4e9a-bea0-0b65b42b2ea0	30km
18305353-de43-46dc-82a4-7f1c2af65b97	99158f03-1565-4e9a-bea0-0b65b42b2ea0	하프(21.0975km)
d05684c2-47a7-4c7d-8488-1aa645093000	99158f03-1565-4e9a-bea0-0b65b42b2ea0	10km
87fe79ee-23dd-43cb-9df5-015b0b10a28b	99158f03-1565-4e9a-bea0-0b65b42b2ea0	5.18km
6c9067f3-6afb-4eb0-b154-9b92c29acc77	99158f03-1565-4e9a-bea0-0b65b42b2ea0	2km
fcebdc8e-d41f-42cc-9c0c-cb4dcbf21eb0	53823ffa-e1f7-40b6-998a-6646d4fb2b56	60km
3e966d68-1981-476d-8265-09ce34bf9cc3	53823ffa-e1f7-40b6-998a-6646d4fb2b56	44km
7029bac5-5918-4a67-8a41-669201781103	a4f9f0bb-ec83-4570-930a-718eeb4e0db8	풀(42.195km)
021d1c3d-eef7-48c7-a522-9b698fc60f2a	a4f9f0bb-ec83-4570-930a-718eeb4e0db8	하프(21.0975km)
6eac2fc3-0d63-49a5-879a-1a00678a9cf8	a4f9f0bb-ec83-4570-930a-718eeb4e0db8	10km
7958c54e-d0fc-4f77-8ba2-bf62891f528c	a4f9f0bb-ec83-4570-930a-718eeb4e0db8	5km
a93f7d04-cac2-4c76-8610-c779c61f4577	d7fb76cb-ccb7-4ca8-915e-3510a70488af	하프(21.0975km)
aaa71ad5-1c0b-4cde-9872-cf12d1207c5c	d7fb76cb-ccb7-4ca8-915e-3510a70488af	10km
de3fd55a-b662-407b-be3a-c4a028d67a71	d7fb76cb-ccb7-4ca8-915e-3510a70488af	5km
738c4044-a9d4-44da-953d-e0fa36d72adf	559eacd6-fdbd-4bba-bf94-2203e387335e	풀(42.195km)
d186491a-98c2-44b4-9754-50636b0b92e9	559eacd6-fdbd-4bba-bf94-2203e387335e	하프(21.0975km)
fc3d7dc9-12cb-4c55-a634-92fd6234b707	559eacd6-fdbd-4bba-bf94-2203e387335e	10km
7e40b7e1-d708-4d66-bf82-82cf0feb9d24	559eacd6-fdbd-4bba-bf94-2203e387335e	5km
501ba96b-5497-4687-a385-8c592e170f24	559eacd6-fdbd-4bba-bf94-2203e387335e	하프(21.0975km)
e6c33428-811b-466c-ab7b-7244c814c574	51f4ab6d-1b50-42af-8aae-60c5fb145533	풀(42.195km)
d3600502-36ce-4f32-a8d0-9fc53a0101b4	51f4ab6d-1b50-42af-8aae-60c5fb145533	하프(21.0975km)
f3b50234-dc74-448a-80e0-3dd396e497df	51f4ab6d-1b50-42af-8aae-60c5fb145533	10km
e4c85e5e-2427-4080-88b4-66ecf95237ba	51f4ab6d-1b50-42af-8aae-60c5fb145533	5km
593552bb-9ab0-4c29-866c-524d3ccbb659	163c9222-99bb-47cb-822e-e0045f8dd9c0	하프(21.0975km)
e09f5115-0d45-43fb-942a-4e9c289f6d78	163c9222-99bb-47cb-822e-e0045f8dd9c0	10km
0dc898b1-e2a4-4d54-8d68-30a2b681ddac	163c9222-99bb-47cb-822e-e0045f8dd9c0	5km건강달리기
eb0bec83-8e45-408f-a55c-375fc9e6b4f4	163c9222-99bb-47cb-822e-e0045f8dd9c0	하프(21.0975km)
c70ea009-470a-41ec-b46a-7ba982f8ebe9	86c2fe4a-5897-45ac-a202-a7f806c96711	하프(21.0975km)
8331cd7a-55b9-4a05-8d9b-741fd5b9f47a	86c2fe4a-5897-45ac-a202-a7f806c96711	10km미니코스
3bce71ed-4e40-4f8e-af0d-21744783d33f	86c2fe4a-5897-45ac-a202-a7f806c96711	5km건강코스
177f1b5a-c88b-4c81-b594-31f4d4517110	c17f27a2-5718-41f4-b69d-7fa63fd03049	10km
85f75779-093a-48c4-9227-42debfd86e90	c17f27a2-5718-41f4-b69d-7fa63fd03049	5km
61d2569c-8f06-44fe-8c32-bcfe3414ffb0	241925e2-44b9-4c5e-b713-44118f37f3bc	풀(42.195km)
ef1300ea-8d5c-4519-b410-72abbd2bc093	241925e2-44b9-4c5e-b713-44118f37f3bc	10km
f62e3ed7-a8e3-4870-bbfe-a0a4a8af2aba	462204bc-793f-4abf-89d2-9d730e4ade28	하프(21.0975km)
241623ab-352c-48b9-9aa4-4eebd910e6ad	462204bc-793f-4abf-89d2-9d730e4ade28	10km
9539861e-6bb5-4636-b63d-48499f92c5f2	462204bc-793f-4abf-89d2-9d730e4ade28	5km
c912e1dc-ccb1-45a5-ba04-a22fac9445ca	fc3468d2-b9a1-41f9-be91-08c39eaddbdf	풀(42.195km)
ca90644c-0f05-4339-b390-a2f4c28174e9	fc3468d2-b9a1-41f9-be91-08c39eaddbdf	하프(21.0975km)
173e2794-105d-4e0d-be35-48f105200e39	fc3468d2-b9a1-41f9-be91-08c39eaddbdf	10km
91391e43-79d1-4282-bfd3-8a187b91d019	fc3468d2-b9a1-41f9-be91-08c39eaddbdf	4km
\.


--
-- TOC entry 3489 (class 0 OID 25987)
-- Dependencies: 215
-- Data for Name: marathons; Type: TABLE DATA; Schema: public; Owner: runhwani
--

COPY public.marathons (marathon_id, name, date, location, created_at) FROM stdin;
436d4548-5bee-4052-b680-ebff7e6d27d4	3.1절 기념 제74회 단축마라톤	2025-03-01 09:00:00+00	인천대학교 송도캠퍼스	2025-05-21 04:08:04.913942+00
13d49c17-ca87-453e-9b21-d013d31bfc26	제26회 3.1절 건강달리기	2025-03-01 09:00:00+00	강원일보 본사	2025-05-21 04:08:09.524634+00
e608bf56-0ee9-4511-9668-25278a826529	머니투데이방송 31절 마라톤대회	2025-03-01 09:00:00+00	뚝섬한강공원 수변무대	2025-05-21 04:08:12.381453+00
be568422-fee9-4caa-9669-06b7791cdbf5	서울 관악산트레일	2025-03-02 09:00:00+00	낙성대공원	2025-05-21 04:08:15.395694+00
02950fae-2e6c-4128-a2e6-302bcfdbd391	제60회 광주일보 3.1절 전국마라톤	2025-03-02 09:00:00+00	화순파크골프장	2025-05-21 04:08:18.720766+00
72d76a0f-693a-4243-b98c-3242d970d455	2025 수원국제하프마라톤	2025-03-02 09:00:00+00	수원종합운동장	2025-05-21 04:08:22.705493+00
97e07f48-d254-40d6-b1ea-2fd95c1cfa37	2025 버킷런	2025-03-02 09:00:00+00	여의도한강공원 이벤트광장	2025-05-21 04:08:28.355886+00
0bdec990-e127-4cb5-9d3a-e309e33923c1	2025 구미박정희마라톤	2025-03-02 09:00:00+00	구미시민운동장	2025-05-21 04:08:30.938768+00
438963b0-7ceb-4a3a-89f2-339ad40aee65	오렌지 런 2025	2025-03-08 09:00:00+00	여의도공원 문화의마당	2025-05-21 04:08:35.746453+00
b293ebfa-6f4d-4318-a7b1-03780cee67a9	제3회 코리아오픈 레이스	2025-03-08 09:00:00+00	뚝섬한강공원 수변무대	2025-05-21 04:08:40.203357+00
42f754eb-cfc9-44df-a066-a770a88671bc	제20회 부산 비치울트라 마라톤 대회	2025-03-08 09:00:00+00	다대포 해수욕장 주차장	2025-05-21 04:08:44.354109+00
92d476c4-51ec-462c-87d8-1457c2d33327	마라톤52 투게더 레이스	2025-03-09 09:00:00+00	우이천 다목적 광장	2025-05-21 04:08:47.665127+00
6995f3e2-3315-4419-9b08-5992aca95820	제15회 여의도 벚꽃마라톤대회	2025-03-09 09:00:00+00	여의도한강공원 이벤트광장	2025-05-21 04:08:53.342229+00
8e0cd2a4-03fb-4cac-9150-1c2ff3965ba8	2025 MBN 블루레이스 거제	2025-03-09 09:00:00+00	거제스포츠파크	2025-05-21 04:08:57.920775+00
f9aba6df-35f6-4fbe-bbcf-7a4f3c5871aa	2025 성주 참외 전국마라톤	2025-03-09 09:00:00+00	성주별고을운동장	2025-05-21 04:09:03.566534+00
1291a67e-30aa-41cc-a32e-577c1584a963	정읍동학마라톤대회	2025-03-09 09:00:00+00	정읍시종합경기장	2025-05-21 04:09:06.822955+00
1c845ba9-8928-432c-82e3-c6e6af11b70b	제2회 불패마라톤	2025-03-15 09:00:00+00	여의도공원 문화의마당	2025-05-21 04:09:11.240361+00
b1812afd-276a-4ad7-be49-b301a99d324f	제19회 창녕부곡온천마라톤	2025-03-15 09:00:00+00	창녕스포츠파크	2025-05-21 04:09:13.616724+00
74118f85-1de9-453c-b5ce-004a809ece2b	2025 서울마라톤 (동아 마라톤)	2025-03-16 09:00:00+00	광화문광장, 잠실종합운동장	2025-05-21 04:09:18.456773+00
2b4b75fb-9c43-4701-9741-5dc9227703db	마라톤52 투게더 레이스	2025-03-22 09:00:00+00	우이천 다목적 광장	2025-05-21 04:09:23.38906+00
078f5822-068f-42e4-9742-a845d6a4a816	2025 성남런페스티벌	2025-03-22 09:00:00+00	성남 탄천종합운동장	2025-05-21 04:09:27.263908+00
19209c85-d927-483c-92fe-3004d566e6a7	2025 지리산 봄꽃레이스	2025-03-22 09:00:00+00	구례 토지조등학교	2025-05-21 04:09:30.082398+00
db9ce38d-41a0-4716-8479-cc37766f4c6f	2025 부천시의회 의장기 10km 마라톤	2025-03-23 09:00:00+00	부천 상동호수공원	2025-05-21 04:09:34.850486+00
ac2b9a84-84b4-49d0-ab1d-141d2b977ea3	2025 무해런	2025-03-23 09:00:00+00	여의도 한강공원 물빛무대	2025-05-21 04:09:39.185985+00
33423f3e-f32f-4d0f-871d-80641d434b66	제32회 315마라톤	2025-03-23 09:00:00+00	3.15 해양누리공원	2025-05-21 04:09:44.551982+00
7bd901df-eb7f-4292-a0ed-31c489d94270	제12회 남산우정 마라톤 대회	2025-03-23 09:00:00+00	장춘단 야구장 앞	2025-05-21 04:09:49.315068+00
97b763cf-1123-4b14-893b-4d2917801203	제23회 성우하이텍배 KNN 환경마라톤	2025-03-23 09:00:00+00	BEXCO 제1전시장 3홀	2025-05-21 04:09:54.872189+00
740bc1c4-93ba-489f-bf7d-28ca3ea201a3	2025 전마협 금산마라톤대회	2025-03-23 09:00:00+00	금산인삼엑스포주차장	2025-05-21 04:09:59.441499+00
df9a46f1-b5e3-456f-8d88-c6375451034f	부산 영도 태종대트레일	2025-03-23 09:00:00+00	남항대교 수변야외공연장	2025-05-21 04:10:03.196977+00
60ff3524-3777-4f70-883a-46e2c25fe99d	제22회 태화강 국제마라톤	2025-03-29 09:00:00+00	태화강 국가정원 야외공연장	2025-05-21 04:10:06.869787+00
49e6cd47-bfc1-4d61-9c44-94530c2ef12a	2025 은평 불광천 벚꽃마라톤	2025-03-30 09:00:00+00	불광천 수변무대	2025-05-21 04:10:11.34455+00
62121e18-92d3-4197-8431-0450724f7ffd	마라톤52 투게더 레이스	2025-03-30 09:00:00+00	우이천 다목적 광장	2025-05-21 04:10:16.655334+00
eba86a64-699d-457c-97d6-429fcc159325	2025 한강 벚꽃마라톤	2025-03-30 09:00:00+00	상암 월드컵공원 평화광장	2025-05-21 04:10:19.59727+00
bf07af3f-97e8-4c72-8012-7d56c362bfcd	2025 전마협 무안 해변마라톤	2025-03-30 09:00:00+00	무안낙지공원	2025-05-21 04:10:23.259562+00
076e57fd-5809-4c36-bdf5-c63dbd4cd846	제24회 합천벚꽃마라톤	2025-03-30 09:00:00+00	합천공설운동장	2025-05-21 04:10:27.743259+00
fed0bd70-c78d-46a1-bdff-0197396c268d	제25회 인천국제하프마라톤	2025-03-30 09:00:00+00	인천문학경기장	2025-05-21 04:10:32.971074+00
3cfc3332-6868-4666-94a6-72aa627e5967	제19회 정남진 장흥마라톤	2025-03-30 09:00:00+00	장흥군 탐진강	2025-05-21 04:10:38.31275+00
e724f721-0f9f-46c9-83b2-6b2f2f58364a	제16회 대한경제 마라톤대회	2025-04-05 09:00:00+00	상암 월드컵공원 평화의광장	2025-05-21 04:10:52.085804+00
854b87cc-a6a3-4c98-844f-8ffb8c6d67b7	제10회 기적의 마라톤	2025-04-05 09:00:00+00	엑스포 수상공원	2025-05-21 04:10:56.250033+00
ca0be125-6304-4ac4-ab94-c273a5988c54	제32회 경주벚꽃마라톤	2025-04-05 09:00:00+00	보덕동 행정복지센터	2025-05-21 04:11:01.896102+00
c62c8fe5-2583-4c1e-9c21-7670e3cb18e5	제12회 메르세데스 벤츠 기브앤레이스	2025-04-06 09:00:00+00	벡스코 야외광장	2025-05-21 04:11:05.902412+00
6721a9af-1ae5-4af7-8f73-77a56aa2fccf	제20회 구로구연맹회장배 마라톤	2025-04-06 09:00:00+00	구일역	2025-05-21 04:11:11.408687+00
5793599d-8863-4b2a-97ca-8d7b176b9ee5	국립창원대와 함께하는 제1회 벚꽃마라톤	2025-04-06 09:00:00+00	국립창원대학교운동장	2025-05-21 04:11:14.393992+00
cb3f2398-ce3c-4f0e-a77e-06a51f7442d2	제21회 예산윤봉길전국마라톤	2025-04-06 09:00:00+00	예산종합운동장	2025-05-21 04:11:19.776539+00
6934bfde-8b72-4180-becb-0e38eabc5493	2025 군산새만금마라톤	2025-04-06 09:00:00+00	군산월명종합운동장	2025-05-21 04:11:24.837926+00
6173f494-e5e7-4e12-be35-5ddebab1e2d5	목포 유달산 마라톤 2025	2025-04-06 09:00:00+00	목포 종합경기장	2025-05-21 04:11:29.691348+00
bc667145-2dfe-4c07-961b-deb3bc9074a0	통영 한산도 트레일런	2025-04-06 09:00:00+00	통영항여객선터미널	2025-05-21 04:11:32.81241+00
eb9eaed7-532a-4afc-8d18-254329967912	2025 진주남강마라톤대회	2025-04-06 09:00:00+00	신안 평거 남강둔치 진양호	2025-05-21 04:11:35.760188+00
b454082c-1d0a-49e2-ba65-ae4bdd332c42	2025 서울봄꽃레이스	2025-04-06 09:00:00+00	신정교하부육상트랙구장	2025-05-21 04:11:40.749311+00
49e65e42-44aa-4c39-a975-16efb7e28c04	제26회 이천 도자기 마라톤 대회	2025-04-06 09:00:00+00	이천종합운동장	2025-05-21 04:11:45.809033+00
ee149d7c-b911-457d-b0fb-7dd2f6263c63	2025 고양특례시 하프마라톤	2025-04-06 09:00:00+00	고양종합운동장	2025-05-21 04:11:51.304293+00
4b21a4c9-a706-4b95-b387-f08ba97e2dd0	제11회 나주영산강마라톤	2025-04-06 09:00:00+00	나주종합스포츠파크	2025-05-21 04:11:54.421121+00
3f791269-6fe2-4557-83f1-740d1c44f5e3	2025 영주 소백산 마라톤	2025-04-06 09:00:00+00	영주시민운동장	2025-05-21 04:11:57.417161+00
b0658c63-86dc-4101-aeab-6863467845c6	제7회 망우산 임도산불조심 트레일 러닝	2025-04-06 09:00:00+00	사가정공원	2025-05-21 04:11:59.949973+00
01186fd7-308d-40a0-a40d-e14188fdc129	제10회 서울트레일런	2025-04-12 09:00:00+00	서울시청 앞 덕수궁 돌담길	2025-05-21 04:12:03.500175+00
741c9789-64f8-464f-9e8e-bac0b367888a	대청호 벚꽃길 마라톤	2025-04-12 09:00:00+00	동구 벚꽃한터	2025-05-21 04:12:07.36801+00
828b4788-0336-426e-8583-9c9a43904c18	제22회 전기사랑마라톤대회	2025-04-12 09:00:00+00	하남 미사경정공원	2025-05-21 04:12:11.396907+00
a49744cd-09dd-4630-836a-3f0593edf0a8	제21회 청남대울트라마라톤	2025-04-12 09:00:00+00	청남대	2025-05-21 04:12:15.662974+00
34e257d3-3640-4981-8fbf-66b5fb63176f	제17회 여명국제마라톤	2025-04-12 09:00:00+00	뚝섬한강공원 수변광장	2025-05-21 04:12:19.629401+00
f54eb121-5fe6-4260-b690-ccc74513f0ba	제14회 양천마라톤대회	2025-04-12 09:00:00+00	안양천 해마루축구장	2025-05-21 04:12:23.563201+00
3c514ad6-1279-413a-9f03-bfcc5cb04b99	제14회 MBC 섬진강 꽃길 마라톤	2025-04-13 09:00:00+00	섬진강생활공원주차장	2025-05-21 04:12:28.348952+00
84bb8a86-5c9c-46cb-ab47-c7fba1f99130	마라톤52 투게더 레이스	2025-04-13 09:00:00+00	우이천 다목적 광장	2025-05-21 04:12:31.796052+00
bd8190e1-b3d2-4bdc-be47-90d7d9c8b2e5	제13회 김포한강 마라톤	2025-04-13 09:00:00+00	김포종합운동장	2025-05-21 04:12:35.532705+00
f8583c2f-5c2f-4923-8008-8e1dc2f9e8e8	중랑마라톤 2025	2025-04-13 09:00:00+00	이화교 중화체육공원	2025-05-21 04:12:39.200271+00
6f93e4c0-87c6-4249-a2aa-e1ff55eaae58	제21회 창원야철마라톤	2025-04-13 09:00:00+00	로봇랜드	2025-05-21 04:12:42.125565+00
f48a2ad0-84ae-42f0-8f11-a65575244fee	2025 서울 YMCA 마라톤대회	2025-04-13 09:00:00+00	광화문 광장	2025-05-21 04:12:45.833325+00
1575f183-d421-4946-976d-b9359bccc99a	제2회 해평마라톤 대회	2025-04-13 09:00:00+00	남원 사랑의광장	2025-05-21 04:12:49.17919+00
282a6eca-5468-4ca1-8536-55194c22ed69	소외계층돕기 제13회 행복한가게 마라톤대회	2025-04-13 09:00:00+00	여의도 한강공원 이벤트광장	2025-05-21 04:12:54.902175+00
71369930-94be-4087-9a23-f53460308657	제8회 광진구청장배 육상대회	2025-04-13 09:00:00+00	뚝섬한강공원 수변무대	2025-05-21 04:12:59.628058+00
8947549e-267b-4b3b-ad83-7dcdb6a48c82	제29회 삼척 황영조 국제 마라톤대회	2025-04-13 09:00:00+00	삼척엑스포광장	2025-05-21 04:13:04.041373+00
8c8750cb-6a0a-43f4-a65b-3acad4f8f3a9	제18회 영남일보 국제 하프마라톤	2025-04-13 09:00:00+00	대구스타디움	2025-05-21 04:13:07.602162+00
9a96ae3e-95b0-4272-b658-5ee470cb1504	대구팔공산 소능종주32k	2025-04-13 09:00:00+00	칠곡 소야고개	2025-05-21 04:13:12.539427+00
7c8e4788-694a-4f75-ad12-3691d7ed6924	제6회 런포더문 2025 기부런 캠페인	2025-04-14 09:00:00+00	버추얼런 캠페인	2025-05-21 04:13:16.266318+00
bb6b43e7-8e2d-4050-a89c-b0153ecadf4b	2025 키움런	2025-04-19 09:00:00+00	여의도 한강공원 물빛무대	2025-05-21 04:13:19.673987+00
cb524828-12fd-43c5-b879-7fe42b02a406	마라톤52 투게더 레이스	2025-04-19 09:00:00+00	우이천 다목적 광장	2025-05-21 04:13:23.410833+00
6dcaff10-7eba-46f9-992d-f051a1ba5762	2025 MBN 선셋마라톤 in 영종	2025-04-19 09:00:00+00	영종도 씨사이드파크	2025-05-21 04:13:28.540883+00
a8723304-a1f8-4ea2-af6f-679a05861553	제1회 마포 서윤복 마라톤대회	2025-04-19 09:00:00+00	상암 월드컵공원 평화광장	2025-05-21 04:13:32.100977+00
44c45bfe-0260-4cfb-971f-500648c14728	제2회 아산 신정호 트레일런 울트라마라톤	2025-04-19 09:00:00+00	아산 신정호 국민관광지	2025-05-21 04:13:35.295355+00
da8b9f0d-9ea3-4233-a664-57ef41331972	내포 마라톤 2025	2025-04-19 09:00:00+00	충남도서관사거리	2025-05-21 04:13:38.788771+00
72e060a5-95c3-4d96-ab07-caaa32fb85cc	제19회 2025 단양팔경 걷기 및 마라톤대회	2025-04-20 09:00:00+00	단양생태체육공원	2025-05-21 04:13:46.680426+00
cea0ce6b-1d18-44f0-96b2-b644e464d936	제23회 울산커플마라톤	2025-04-20 09:00:00+00	울산대공원 남문광장	2025-05-21 04:13:49.867021+00
70ad0e67-1482-4954-904c-6e36b0f4c13d	아트밸리 아산 제3회 이순신 백의종군길 마라톤	2025-04-20 09:00:00+00	은행나무길 제2주차장	2025-05-21 04:13:53.665128+00
c3f2566e-c83e-445a-91a5-e87463c71bfa	2025 DMZ 평화마라톤	2025-04-20 09:00:00+00	임진각 평화누리 공연장	2025-05-21 04:13:57.555573+00
2b891150-80c9-4e50-b1d9-81b30312acee	부안해변마라톤 2025	2025-04-20 09:00:00+00	부안군 변산해수욕장	2025-05-21 04:14:02.623081+00
7e5e7a1c-49f7-4142-8bf9-0c5c0c02d309	제9회 노원구청장배 및 제8회 회장배 마라톤	2025-04-20 09:00:00+00	창동교 나눔의광장	2025-05-21 04:14:05.917273+00
829cf6e3-09ae-4255-b63e-f664095f3848	제23회 경기마라톤	2025-04-20 09:00:00+00	수원종합운동장	2025-05-21 04:14:09.237336+00
424f5ba2-cf0b-4d99-b379-d364fceae67d	제22회 호남마라톤	2025-04-20 09:00:00+00	승촌보 영산강 문화관	2025-05-21 04:14:12.239281+00
1c4f2013-6022-41ec-92d4-8b17f41f7e5b	2025 서울클린뷰티런	2025-04-26 09:00:00+00	한강 노들섬	2025-05-21 04:14:16.267539+00
3952afe8-f8bf-4c65-bec3-aa5244ccc749	2025 국민고향 남해 마시고 RUN	2025-04-26 09:00:00+00	창선생활체육공원	2025-05-21 04:14:19.688004+00
b7d4cb72-70e6-4fc2-894c-a74788a64fba	제2회 리사이클 환경마라톤	2025-04-26 09:00:00+00	상암 평화의공원 평화광장	2025-05-21 04:14:22.984937+00
df12f41e-eca5-4b66-96ec-f27e3264ec18	빵빵런 2025	2025-04-26 09:00:00+00	뚝섬한강공원 수변무대	2025-05-21 04:14:26.453345+00
d5a23196-d431-4978-8d25-2d62c2551e2e	제13회 광명시 육상연맹 회장배 건강달리기	2025-04-27 09:00:00+00	안양천 광명찬빛광장	2025-05-21 04:14:30.377153+00
3a8f97ff-9ea5-4b4e-b957-54d46f5e44de	제21회 대전 3대하천 마라톤대회	2025-04-27 09:00:00+00	대전엑스포시민광장	2025-05-21 04:14:35.112387+00
0f6029be-29df-418b-bf83-b516e9ad25ff	마라톤52 투게더 레이스	2025-04-27 09:00:00+00	우이천 다목적 광장	2025-05-21 04:14:38.642195+00
0870a4ce-5d34-460e-a41d-d44b5f4f6964	2025 홍천트레일런	2025-04-27 09:00:00+00	홍천생명과학관	2025-05-21 04:14:42.624401+00
1fc2fe1a-01be-4928-9898-fc0d3345a220	제7회 연합뉴스 기장바다마라톤	2025-04-27 09:00:00+00	오시리아 물음표공원 일원	2025-05-21 04:14:47.485665+00
679cb0d4-7dd7-4eb2-ac81-72d6ed4f7de3	제23회 통일기원포항해변마라톤	2025-04-27 09:00:00+00	포항종합운동장	2025-05-21 04:14:51.798878+00
54746e65-757c-4970-99cd-d592d85877fb	제19회 반기문마라톤대회	2025-04-27 09:00:00+00	음성종합운동장	2025-05-21 04:14:56.056947+00
664dc299-0766-4817-a0df-dbebc926af54	2025 서울하프마라톤	2025-04-27 09:00:00+00	광화문광장	2025-05-21 04:14:59.356405+00
3ce29726-7ee2-4aeb-90fc-e76d8f95b703	2025 평화의 섬 제주국제마라톤	2025-04-27 09:00:00+00	제주대학교 대운동장	2025-05-21 04:15:03.765231+00
7640d06e-30b6-4443-96c0-e8901c20d1a6	서울 청계산트레일런	2025-04-27 09:00:00+00	청계산근린광장 공영주차장	2025-05-21 04:15:08.087166+00
30969e1e-7378-4f80-b372-0bb28c814258	제25회 여성마라톤	2025-05-03 09:00:00+00	상암 평화의공원 평화광장	2025-05-21 04:15:11.684006+00
552a9646-f2f2-44ed-bb0f-87eecc4926b5	2025 서울 유아차 런	2025-05-03 09:00:00+00	광화문광장	2025-05-21 04:15:16.153353+00
fa8cc26e-8211-4b65-8625-f9c6d661a63a	제20회 보성 녹차 마라톤	2025-05-03 09:00:00+00	보성공설운동장	2025-05-21 04:15:20.457597+00
5dd75106-a410-4247-be67-0de22b0e6ef4	제23회 서울 불,수,사,도,북 5개산 종주	2025-05-03 09:00:00+00	원자력병원 (불암산 백세문)	2025-05-21 04:15:23.566286+00
d76053e5-8191-4f72-a133-6fdd8ffd17d8	2025 한강 서울 하프 마라톤	2025-05-04 09:00:00+00	상암 월드컵공원 평화광장	2025-05-21 04:15:28.292445+00
45175919-7f76-4202-b38b-8080a27df0d3	2025 우리은행배 제물포르네상스 국제 마라톤	2025-05-04 09:00:00+00	상상플랫폼	2025-05-21 04:15:33.566462+00
01b9229e-420c-4838-9ee8-e9578582822b	밀양 아리랑트레일런	2025-05-04 09:00:00+00	밀양보트장	2025-05-21 04:15:37.141053+00
db2b1598-6589-4826-9cfe-d577bb347028	제22회 부산마라톤	2025-05-04 09:00:00+00	대저생태공원	2025-05-21 04:15:42.325603+00
cd08e775-6dbf-4f94-aa03-b878f5acfebc	소아암환우돕기 제22회 서울시민마라톤	2025-05-04 09:00:00+00	여의도 한강공원 이벤트광장	2025-05-21 04:15:44.777994+00
e60f5504-de56-4717-a72e-ff596b5104ae	제10회 기적의 마라톤 with 이봉주	2025-05-05 09:00:00+00	엑스포수상공원	2025-05-21 04:15:49.61171+00
dd28d251-aea1-440b-b715-db0cc0eac51a	버닝런 2025	2025-05-06 09:00:00+00	여의도한강공원 이벤트광장	2025-05-21 04:15:53.98503+00
78a95536-5f5f-476a-94c5-1058185ae73a	제1회 화이트런	2025-05-10 09:00:00+00	상암 월드컵공원 평화광장	2025-05-21 04:15:58.806772+00
af4547de-5912-47d2-aff6-b8023c596f88	제26회 화성 효 마라톤대회	2025-05-10 09:00:00+00	화성종합경기타운	2025-05-21 04:16:03.360518+00
1662767b-b1f7-46a9-9429-5c0dd2c0e22b	2025 도시가스 온런 광주	2025-05-10 09:00:00+00	광주월드컵경기장	2025-05-21 04:16:07.258094+00
ac792086-5090-402f-af03-e26cf8431a26	2025 가정의달 하하호호마라톤	2025-05-10 09:00:00+00	월드컵공원 평화광장	2025-05-21 04:16:11.499224+00
54e95088-9864-40bf-8c7a-0fa86f0ee9ba	제4회 전국 무등산 무돌길 완주대회	2025-05-10 09:00:00+00	광주역 광장	2025-05-21 04:16:20.580994+00
decc0283-05ce-402f-a5df-e1c75f520c8d	제18회 대전한밭벌울트라마라톤	2025-05-10 09:00:00+00	대전엑스포 시민광장	2025-05-21 04:16:26.264016+00
b1739738-0d9c-486a-97dd-0ea6ebe77639	2025 금산무료초청마라톤	2025-05-11 09:00:00+00	금산인삼엑스포주차장	2025-05-21 04:16:28.729716+00
6225d03e-1d13-4e6f-bdeb-8e9ecd7b0ac1	제22회 여주 세종대왕 마라톤대회	2025-05-11 09:00:00+00	여주종합운동장	2025-05-21 04:16:33.945752+00
438f68fd-d85b-432c-9e6b-75925f9b01f9	제1회 전주마라톤	2025-05-11 09:00:00+00	전주천 야외무대	2025-05-21 04:16:39.277039+00
53eb9e41-2371-4d55-bb47-1f0d5de13c7e	2025 전국의병 마라톤	2025-05-11 09:00:00+00	의령공설운동장	2025-05-21 04:16:43.47497+00
dfdc5b32-a5f2-4267-aa9e-b35b64dfa99c	제2회 튼튼이마라톤 2025	2025-05-11 09:00:00+00	상암 월드컵공원 평화의 광장	2025-05-21 04:16:48.327204+00
2068190e-99bc-4222-8482-cc19c5670330	컬처런 2025 인천영종국제도시 마라톤	2025-05-17 09:00:00+00	씨사이드파크 하늘구름광장	2025-05-21 04:16:52.220515+00
9937b561-a80c-4e8c-b323-0e761cb25fe9	안산청년회의소 회장배 마라톤대회	2025-05-17 09:00:00+00	안산호수공원 일대	2025-05-21 04:16:56.409377+00
09d000ce-4a14-4dc9-8657-58199be8264d	2025 서울신문 하프마라톤	2025-05-17 09:00:00+00	월드컵공원 평화의광장	2025-05-21 04:17:09.168184+00
86c6837b-49a8-4e87-b092-ec32c7564bbd	제3회 서울한강울트라마라톤	2025-05-17 09:00:00+00	우이천 다목적 광장	2025-05-21 04:17:11.6269+00
8697f96a-347c-4c19-b7aa-e09093edabc8	2025 굽네 오븐런 5K	2025-05-18 09:00:00+00	상암 평화의공원 평화광장	2025-05-21 04:17:16.186835+00
dfca79f7-27ca-479e-804b-0c8c895f3065	제3회 여의도 밤섬마라톤	2025-05-18 09:00:00+00	여의도공원 문화의마당	2025-05-21 04:17:19.423602+00
129b9441-f4c9-4457-8656-223c002843e2	2025 무의도구석구석 국제트레일런	2025-05-18 09:00:00+00	하나개 해수욕장	2025-05-21 04:17:24.082406+00
62aa9950-b270-4c9f-b995-d5cb1c91f193	제15회 대전 서구청장배 마라톤대회	2025-05-18 09:00:00+00	대전엑스포시민광장	2025-05-21 04:17:27.12795+00
2c23bbe7-12da-417c-bc20-3d2ff9dd2a78	2025 라이트업! 키즈레이스	2025-05-24 09:00:00+00	강남 대치유수지 체육공원	2025-05-21 04:17:31.969653+00
7c0e4cc9-7eb2-4389-9532-ba0389c854e7	제22회 보령머드 임해마라톤	2025-05-24 09:00:00+00	대천해수욕장 제2주차장	2025-05-21 04:17:35.314222+00
49e11052-b352-47af-bb37-55bab98d7146	2025 컬러레이스	2025-05-24 09:00:00+00	렛츠런파크 부산경남에코랜드	2025-05-21 04:17:42.087649+00
9b8f40d4-7e04-4521-a3cb-32070a2a7a55	제30회 바다의 날 마라톤	2025-05-24 09:00:00+00	상암 월드컵공원 평화광장	2025-05-21 04:17:46.539511+00
e531c8ff-79e7-42d7-9f4d-eaf7d14678f5	제25회 5.18마라톤대회	2025-05-24 09:00:00+00	국립5.18민주묘지 광장	2025-05-21 04:17:50.812216+00
1a6b7b21-d5f8-4d6b-8e67-971661eaa3c4	2025 옥스팜 트레일워커	2025-05-24 09:00:00+00	인제군 일대	2025-05-21 04:17:54.567625+00
150ba389-9d17-49d9-92d0-78574e257f0f	제2회 남해 다랑논 마라톤	2025-05-24 09:00:00+00	회룡 농촌체험 휴양마을	2025-05-21 04:17:58.485127+00
3364bc65-6806-4ebd-b1f9-0149f61b8c2f	2025 구리 유채꽃 마라톤	2025-05-25 09:00:00+00	구리한강시민공원	2025-05-21 04:18:04.455139+00
3929b66d-e1d1-4af7-8994-9edad8065207	제19회 강북구청장배 마라톤	2025-05-25 09:00:00+00	우이천 한일병원 맞은편	2025-05-21 04:18:08.243761+00
77191212-0d8f-4277-8406-d1abb6820970	제5회 영등포구청장배 육상대회	2025-05-25 09:00:00+00	안양천 신정교 육상트랙구장	2025-05-21 04:18:11.803893+00
762e9377-607f-4da9-a536-a553dc281314	2025 KTX 광명역 평화 마라톤	2025-05-25 09:00:00+00	KTX 광명역 일원	2025-05-21 04:18:15.567571+00
238df25d-47a1-4b8e-b772-39b8f8a3f05f	제29회 제주국제관광마라톤축제	2025-05-25 09:00:00+00	구좌체육공원	2025-05-21 04:18:18.193974+00
ccd33944-f768-424b-908b-e1b083024197	2025 백양산숲길건강달리기	2025-05-25 09:00:00+00	부산학생교육문화회관	2025-05-21 04:18:22.204458+00
194de71f-fb52-4f26-b90c-ffd3e3bd2851	무주 하프 마라톤 2025	2025-05-25 09:00:00+00	무주등나무운동장	2025-05-21 04:18:26.917971+00
25be7dce-d6ff-449c-9dcd-fd5dd5314034	독도수호를 위한 동호인마라톤대회 2025	2025-05-25 09:00:00+00	경주 서천둔치 생활체육광장	2025-05-21 04:18:30.591374+00
770a2ec0-b7bb-440f-b706-52cb4b135d3f	제35회 지리산 화대종주 CLIMBATHON	2025-05-25 09:00:00+00	화엄사	2025-05-21 04:18:34.957844+00
642e7120-ed24-43e2-aee1-d9e5746e5e46	제18회 강남구 육상연맹회장배 육상대회	2025-05-31 09:00:00+00	대치유수지 체육공원	2025-05-21 04:18:39.320817+00
4a86b226-b645-4bf6-badb-6ffb1f71bbe6	2025 지구런 더 피스로드	2025-05-31 09:00:00+00	여의도공원 문화의마당	2025-05-21 04:18:44.113717+00
eff919b0-5ca6-4355-be21-466330ec7683	2025 한밭 런앤워크 페스티벌	2025-05-31 09:00:00+00	대전 엑스포 시민광장	2025-05-21 04:18:47.317011+00
e264e395-a6bc-4777-85f3-15215b03a90b	제11회 I LOVE 방송대 마라톤대회	2025-05-31 09:00:00+00	상암 평화의공원 평화광장	2025-05-21 04:18:51.506576+00
ed6f9b8c-c7ed-4e70-a917-c42d4e77b931	2025 서천한산모시마라톤대회	2025-05-31 09:00:00+00	서천종합운동장	2025-05-21 04:19:00.642639+00
4828a81f-5334-4465-b32f-a815528ad7e2	커피 빵빵런 in 강릉	2025-06-01 09:00:00+00	강릉 경포호수광장	2025-05-21 04:19:05.40213+00
a5da1a1d-0e81-49a8-9265-9b33877b202a	희망드림 제22회 새벽강변 국제마라톤	2025-06-01 09:00:00+00	상암 월드컵공원 평화광장	2025-05-21 04:19:09.428751+00
41c312bc-c0c3-4d6f-9d13-4f6f5d7d0230	제10회 너릿재마라톤대회	2025-06-01 09:00:00+00	화순 셀레브 카페 입구	2025-05-21 04:19:13.555148+00
6c441c46-c257-4bc1-b045-41d22b324c71	2025 대전 월드런 마라톤 축제	2025-06-01 09:00:00+00	대전엑스포시민광장	2025-05-21 04:19:17.533354+00
be254efb-6169-4a71-9277-f247d3f9b3ba	물사랑 낙동강 200km울트라마라톤대회	2025-06-06 09:00:00+00	을숙도 물문화회관 광장	2025-05-21 04:19:21.441628+00
92cb209a-7ac2-427b-bf55-ea64da9e9ad7	2025 춘천봄내마라톤대회	2025-06-07 09:00:00+00	춘천시청 호반광장 일원	2025-05-21 04:19:25.950491+00
26daa80e-af0e-4e35-9c37-779e77ae5e63	2025 하루런	2025-06-07 09:00:00+00	미사경정공원	2025-05-21 04:19:28.85783+00
c6e821bc-ff40-4e54-af55-feea12ae3f3e	제23회 청광종주 트레일러닝	2025-06-07 09:00:00+00	청계산 산림욕장 입구	2025-05-21 04:19:38.328234+00
12f28e97-767a-47ef-b1dc-26d876e85e59	2025 희망 서울 마라톤	2025-06-07 09:00:00+00	평화의공원 평화광장	2025-05-21 04:19:41.748853+00
3f16fb15-3c1e-472e-9b10-042525458a7b	제27회 양평이봉주마라톤겸 경인일보남한강마라톤	2025-06-07 09:00:00+00	양평강상체육공원	2025-05-21 04:19:46.233995+00
36e7f433-0c88-4d19-ba65-77e287f4ead5	2025 청춘런	2025-06-08 09:00:00+00	상암 월드컵공원 평화광장	2025-05-21 04:19:50.130778+00
6707e8fe-efe3-40c5-ba47-c5ad938e9d0e	2025 제주오름 트레일러닝	2025-06-14 09:00:00+00	유채꽃플라자	2025-05-21 04:19:54.182359+00
34b07196-0420-4c90-b78f-761daa4703c1	2025 빵트레일런	2025-06-14 09:00:00+00	하이원 리조트 잔디광장	2025-05-21 04:19:57.528368+00
ae5e60c4-c741-440f-af63-3c6e77c77276	제20회 울릉도 국제 마라톤대회	2025-06-14 09:00:00+00	울릉예술문화체험장	2025-05-21 04:20:01.148193+00
f832c38b-71cf-4b4a-b08c-6579a8bd56cc	제15회 국민행복마라톤	2025-06-14 09:00:00+00	뚝섬한강공원 수변무대	2025-05-21 04:20:05.429437+00
db95b70d-1e65-4740-97c2-f64cc19097e9	제22회 광주 빛고을 울트라 100km 마라톤	2025-06-14 09:00:00+00	광주광역시청 야외음악당	2025-05-21 04:20:08.378841+00
3bad951a-53c0-4cf9-b8ab-cf28d6618e94	2025 인천광역시육상연맹회장배 마라톤	2025-06-14 09:00:00+00	정서진 아라타워 일원	2025-05-21 04:20:13.104977+00
b1886f12-9e4f-4e59-97e7-30272acf014b	제2회 긍정의힘 마라톤	2025-06-15 09:00:00+00	여의도공원 문화의마당	2025-05-21 04:20:17.233052+00
3b2dc722-9a6d-4e5d-9e91-c9c21ab51594	미니언즈 런 2025 서울	2025-06-15 09:00:00+00	상암 평화의공원 평화광장	2025-05-21 04:20:19.73604+00
5005dee1-27de-41e6-a31e-f23675a6e409	제22회 춘천호반마라톤대회	2025-06-15 09:00:00+00	춘천 송암스포츠타운	2025-05-21 04:20:29.472925+00
88279487-1e8b-4ace-baf2-0fcf5f34b865	2025 스틸런 제9회 포항철강마라톤	2025-06-15 09:00:00+00	포항 영일대 해수욕장	2025-05-21 04:20:33.786886+00
1648b380-d11f-4ede-ba38-d20f702474e6	2025 김해 숲길마라톤	2025-06-15 09:00:00+00	김해운동장	2025-05-21 04:20:38.820161+00
8cea705c-e1b9-440b-9a57-4ed2821a5041	제21회 영덕 해변 전국마라톤대회	2025-06-15 09:00:00+00	영덕군 고래불해수욕장	2025-05-21 04:20:42.504549+00
c12b5959-e634-49ef-b23c-4a7bcfda0b13	대전 보문산트레일	2025-06-15 09:00:00+00	대전 오월드주차장	2025-05-21 04:20:46.322607+00
72daa762-e73e-4f70-958d-7852766c11bb	2025 글로벌 6K 마라톤	2025-06-21 09:00:00+00	6개 도시	2025-05-21 04:20:50.560231+00
2c8e67ef-93c6-42a0-9f62-375bb03d073d	제14회 울산 태화강 울트라마라톤	2025-06-21 09:00:00+00	태화강둔치	2025-05-21 04:20:55.037321+00
ef1a1671-858d-4586-b92f-67898efe2cca	2025 스피드업 라이트 금산 마라톤 대회	2025-06-21 09:00:00+00	금산인삼엑스포주차장	2025-05-21 04:20:59.241613+00
e9ccb0d1-abc1-47bc-a54c-1cb632fa5ded	2025 용인마라톤	2025-06-22 09:00:00+00	용인시청 앞 광장	2025-05-21 04:21:02.749592+00
281d344f-7009-4a63-8c60-f0961d1171b8	람사르습지 밤섬런 2025 (구. 온에어런)	2025-06-22 09:00:00+00	여의도공원 문화의마당	2025-05-21 04:21:05.524684+00
fa61eeda-6ea4-431c-be9a-0b90d35dbb89	서울 북한산트레일	2025-06-22 09:00:00+00	우이공원유원지	2025-05-21 04:21:08.704645+00
1e46bd10-2f9a-423a-946e-06d61ab1820c	2025 블루베리 마라톤축제	2025-06-22 09:00:00+00	대흥초등학교 운동장	2025-05-21 04:21:13.638732+00
13687f3a-a3ef-4588-a871-d8ec83ee28a6	2025 보은 속리산 말티재 힐링 알몸 마라톤	2025-06-28 09:00:00+00	보은군 속리산 말티고개	2025-05-21 04:21:17.795728+00
9f78aa16-c445-48c2-9047-30db8df71cad	2025 행복안전동행 마라톤	2025-06-28 09:00:00+00	여의도공원 문화의마당	2025-05-21 04:21:21.048051+00
7efa7b1c-3878-4b0b-8e67-6d4e4ee36d72	2025 광제산 트레일런	2025-07-13 09:00:00+00	진주 광제산 입구 주차장	2025-05-21 04:21:25.409215+00
ac42ff2e-1dd0-4955-9321-74f4be8dff93	2025 울릉도 국제트레일러닝 UiiT 40K	2025-07-13 09:00:00+00	울릉군공설운동장	2025-05-21 04:21:28.090983+00
ac629528-0f47-487c-8c7f-fea9e4801d9e	2025 제15회 태종대 전국마라톤대회	2025-07-20 09:00:00+00	태종대공원	2025-05-21 04:21:30.954519+00
3b9d4c87-b897-4346-b5c1-e7f87476cf20	제7회 안산 인왕산 북악산 CLIMBATHON	2025-07-20 09:00:00+00	연세대 정문	2025-05-21 04:21:36.532773+00
26014c6c-5454-4c22-9469-c95da5dbf5d4	2025 금수산트레일 러닝대회	2025-08-24 09:00:00+00	청풍리조트 힐하우스	2025-05-21 04:21:40.778721+00
50b25e0e-4a08-4512-9da8-c40e5d427418	제2회 삼척시장배 단방산 숲길 마라톤	2025-08-30 09:00:00+00	삼척 궁촌항	2025-05-21 04:21:43.82268+00
a716e4cb-bf3b-4455-8eab-4c12b83c58e0	제4회 선샤인 런	2025-08-30 09:00:00+00	잠실한강공원 청소년광장	2025-05-21 04:21:47.642687+00
7a0bf3bd-ea58-4c7a-81d1-7329b8acc9a0	제19회 순천만울트라마라톤	2025-09-06 09:00:00+00	순천 동천천변공원	2025-05-21 04:21:52.474329+00
6b0c44ba-c4d0-4783-a475-106c030908d1	2025 울진금강송배 전국마라톤대회	2025-09-14 09:00:00+00	울진종합운동장	2025-05-21 04:22:02.128116+00
1ecb1f65-9e48-4e64-95d0-83509a47a051	2025 금산인삼축제 마라톤	2025-09-14 09:00:00+00	금산인삼엑스포주차장	2025-05-21 04:22:05.080972+00
4ec8e96d-1fb0-4168-afd2-d0aed696c07e	제25회 홍성마라톤	2025-09-20 09:00:00+00	홍성 홍주종합경기장	2025-05-21 04:22:07.713751+00
0e6fc03e-97ce-48b8-a4b9-0eb943671d00	2025 철원DMZ 국제평화마라톤	2025-09-21 09:00:00+00	철원군 고석정	2025-05-21 04:22:11.702431+00
a2ca0905-9a8a-4a3e-a071-6b80fb85e254	제23회 전국부부가족 마라톤대회	2025-09-21 09:00:00+00	전북 전주 월드컵경기장 광장	2025-05-21 04:22:15.681191+00
e5c89de7-de95-435b-8444-a76d9c9c969f	2025 서울어스마라톤대회	2025-09-21 09:00:00+00	광화문 광장	2025-05-21 04:22:19.947506+00
c9f72809-15c9-4166-a18b-ebf4ee63537a	제6회 천안삼거리 흥타령 울트라마라톤	2025-09-27 09:00:00+00	천안삼거리공원	2025-05-21 04:22:23.537087+00
8ecf9efc-9562-4081-be15-a169e4d34d5c	2025 안동마라톤	2025-09-28 09:00:00+00	안동시민운동장	2025-05-21 04:22:26.221535+00
afd9979c-c3b9-47d0-bf2e-fb4ec21108ab	2025 서산 코스모스 황금들녘 마라톤	2025-09-28 09:00:00+00	서산스포츠테마파크	2025-05-21 04:22:29.928054+00
5081e2f1-ebb4-45e8-a9be-971524b92fb7	제18회 가평자라섬 전국마라톤대회	2025-09-28 09:00:00+00	가평종합운동장	2025-05-21 04:22:33.704086+00
6f74f786-cb40-47c5-83c2-d15ca684a799	2025 봉화송이 전국마라톤	2025-09-28 09:00:00+00	봉화공설운동장	2025-05-21 04:22:36.345433+00
cb902274-4e25-430f-b1d0-247277527321	2025 포항2차전지 전국마라톤대회	2025-09-28 09:00:00+00	포항송도해수욕장	2025-05-21 04:22:40.444863+00
6e9041d0-5685-49cf-8d30-64ec4a812b59	제2회 울트라서울	2025-10-03 09:00:00+00	시청 별관 앞 덕수궁 돌담길	2025-05-21 04:22:49.919496+00
99158f03-1565-4e9a-bea0-0b65b42b2ea0	제4회 무등산권 지오 마라톤	2025-10-11 09:00:00+00	화순 금호스파리조트	2025-05-21 04:22:59.635171+00
53823ffa-e1f7-40b6-998a-6646d4fb2b56	제1회 아산 이순신 트레일런	2025-10-11 09:00:00+00	신정호 국민관광지	2025-05-21 04:23:02.275665+00
a4f9f0bb-ec83-4570-930a-718eeb4e0db8	2025 해남땅끝 전국마라톤대회	2025-10-11 09:00:00+00	해남공설운동장 우슬체육공원	2025-05-21 04:23:07.867366+00
d7fb76cb-ccb7-4ca8-915e-3510a70488af	제8회 거제시장배 섬꽃 전국 마라톤	2025-10-12 09:00:00+00	거제스포츠파크 주경기장	2025-05-21 04:23:11.963111+00
559eacd6-fdbd-4bba-bf94-2203e387335e	2025 MBN 전국 나주 마라톤	2025-10-12 09:00:00+00	나주종합스포츠파크	2025-05-21 04:23:15.52318+00
51f4ab6d-1b50-42af-8aae-60c5fb145533	제23회 청원생명쌀 대청호마라톤	2025-10-18 09:00:00+00	문의체육공원	2025-05-21 04:23:18.237674+00
163c9222-99bb-47cb-822e-e0045f8dd9c0	2025 경포마라톤	2025-10-18 09:00:00+00	경포해변 중앙광장	2025-05-21 04:23:23.164073+00
86c2fe4a-5897-45ac-a202-a7f806c96711	제24회 대청호마라톤대회	2025-10-19 09:00:00+00	대청공원	2025-05-21 04:23:27.391021+00
c17f27a2-5718-41f4-b69d-7fa63fd03049	제7회 유성국화마라톤	2025-10-26 09:00:00+00	유림공원 잔디광장 일대	2025-05-21 04:23:31.957041+00
241925e2-44b9-4c5e-b713-44118f37f3bc	2025 JTBC 서울마라톤	2025-11-02 09:00:00+00	상암 월드컵경기장	2025-05-21 04:23:37.935076+00
462204bc-793f-4abf-89d2-9d730e4ade28	제20회 울산인권 마라톤대회	2025-11-16 09:00:00+00	태화강 둔치 (태화로터리)	2025-05-21 04:23:41.822141+00
fc3468d2-b9a1-41f9-be91-08c39eaddbdf	제23회 상주 곶감 마라톤대회	2025-11-16 09:00:00+00	상주시민운동장	2025-05-21 04:23:47.520408+00
\.


--
-- TOC entry 3498 (class 0 OID 26121)
-- Dependencies: 224
-- Data for Name: todos; Type: TABLE DATA; Schema: public; Owner: runhwani
--

COPY public.todos (todo_id, curriculum_id, user_id, content, is_done, date) FROM stdin;
7934ef16-b611-437d-b290-b4d804c1a0c0	72f6038a-7bc5-4b8e-865f-754f191bdd8e	063cf97b-0c40-4c82-a4f7-4f711ea11578	중간 속도 지속주: 2km, 6:45min/km, 목적: 페이스 적응	f	2025-05-22 15:00:00+00
28c17de9-a96a-4df1-8370-672e91e9045f	7576330d-d2f0-420e-a999-5bdd254b79df	417b3d0e-c29e-4840-a043-415f15c45b6b	회복주: 3km, 8:00min/km, 목적: 부상 방지 및 컨디션 점검	f	2025-05-21 15:00:00+00
dc36c2ee-457f-4062-92e6-08ca3dacc751	7576330d-d2f0-420e-a999-5bdd254b79df	417b3d0e-c29e-4840-a043-415f15c45b6b	휴식: 휴식일, 목적: 회복 및 적응	\N	2025-05-22 15:00:00+00
85055252-675e-4c9f-96d2-c14359880235	7576330d-d2f0-420e-a999-5bdd254b79df	417b3d0e-c29e-4840-a043-415f15c45b6b	장거리 지구력주: 5km, 7:30min/km, 목적: 유산소 지구력 향상	f	2025-05-23 15:00:00+00
67a95d41-6f90-4ea0-8b5b-c0c643a8b4f4	7576330d-d2f0-420e-a999-5bdd254b79df	417b3d0e-c29e-4840-a043-415f15c45b6b	회복주: 3km, 8:00min/km, 목적: 피로회복	f	2025-05-24 15:00:00+00
421db730-b7c1-4d46-8adb-0d4bd9ee035e	7576330d-d2f0-420e-a999-5bdd254b79df	417b3d0e-c29e-4840-a043-415f15c45b6b	중간 속도 지속주: 2km, 6:00min/km, 목적: 지구력 및 근지구력 자극	f	2025-05-25 15:00:00+00
e1f4c854-9fe7-48fe-af4b-180da0b6aa17	7576330d-d2f0-420e-a999-5bdd254b79df	417b3d0e-c29e-4840-a043-415f15c45b6b	인터벌: 0.8km x 2회, 5:45min/km(인터벌)/8:30min/km(회복), 목적: 스피드 및 심폐기능 향상	f	2025-05-26 15:00:00+00
3fcda4bb-4760-4c03-a7ed-e53f01aefa78	7576330d-d2f0-420e-a999-5bdd254b79df	417b3d0e-c29e-4840-a043-415f15c45b6b	휴식: 휴식일, 목적: 회복 및 적응	\N	2025-05-27 15:00:00+00
6558ccfe-4d0d-437a-8546-811efd9da316	7576330d-d2f0-420e-a999-5bdd254b79df	417b3d0e-c29e-4840-a043-415f15c45b6b	파틀렉: 4km (1km 예열, 5 x 100m 스퍼트/조깅 반복, 1km 쿨다운), 7:20min/km, 목적: 가변 페이스 적응	f	2025-05-28 15:00:00+00
29b52380-2530-4ec9-997f-d409c8691e9d	7576330d-d2f0-420e-a999-5bdd254b79df	417b3d0e-c29e-4840-a043-415f15c45b6b	회복주: 3km, 8:00min/km, 목적: 피로회복	f	2025-05-29 15:00:00+00
8e6fe97f-8970-48e2-a308-c2fb7b67d7e2	7576330d-d2f0-420e-a999-5bdd254b79df	417b3d0e-c29e-4840-a043-415f15c45b6b	장거리 지구력주: 6km, 7:10min/km, 목적: 거리 적응 및 유산소 능력 확장	f	2025-05-30 15:00:00+00
ad8ea6ce-7b1f-45b7-8647-8b7bb6491624	7576330d-d2f0-420e-a999-5bdd254b79df	417b3d0e-c29e-4840-a043-415f15c45b6b	템포주: 3km, 6:30min/km, 목적: 레이스페이스 적응	f	2025-05-31 15:00:00+00
00815185-6971-4c97-9fd1-927c9271dcc1	7576330d-d2f0-420e-a999-5bdd254b79df	417b3d0e-c29e-4840-a043-415f15c45b6b	휴식: 휴식일, 목적: 회복 및 컨디셔닝	\N	2025-06-01 15:00:00+00
5875df49-6a82-4632-8cab-68782a75adb7	7576330d-d2f0-420e-a999-5bdd254b79df	417b3d0e-c29e-4840-a043-415f15c45b6b	중간 속도 지속주: 3km, 6:00min/km, 목적: 근지구력 및 페이스 유지	f	2025-06-02 15:00:00+00
5332711f-3010-4678-bc50-a43c064f0c5a	7576330d-d2f0-420e-a999-5bdd254b79df	417b3d0e-c29e-4840-a043-415f15c45b6b	인터벌: 0.8km x 3회, 5:45min/km(인터벌)/8:30min/km(회복), 목적: 스피드 및 젖산 역치 강화	f	2025-06-03 15:00:00+00
e51084b0-2db7-41bc-ae73-290f6ceaffd1	7576330d-d2f0-420e-a999-5bdd254b79df	417b3d0e-c29e-4840-a043-415f15c45b6b	회복주: 2km, 8:15min/km, 목적: 피로회복 및 컨디션 점검	f	2025-06-04 15:00:00+00
c5ac2c03-a6b4-4328-b404-f71d5c06c4ef	7576330d-d2f0-420e-a999-5bdd254b79df	417b3d0e-c29e-4840-a043-415f15c45b6b	휴식: 휴식일, 목적: 대회 전 마지막 회복	\N	2025-06-05 15:00:00+00
1db09078-572a-45f1-93e0-42c0925c39de	7576330d-d2f0-420e-a999-5bdd254b79df	417b3d0e-c29e-4840-a043-415f15c45b6b	대회(10km): 10km, 6:20–6:40min/km(목표 페이스), 목적: 완주 및 목표 달성	f	2025-06-06 15:00:00+00
46ffc0ee-3f2c-4c89-8806-f2f1f2d533fe	b14cf416-cdb0-4f4f-a0cf-d593d2d4881a	0f9c3869-310c-48c5-b838-c50fa0ad5bc1	회복주: 5km, 8:00min/km, 목적: 부상 예방 및 컨디션 점검	f	2025-05-21 15:00:00+00
ec446c99-c07c-43dd-966a-45867d4970d4	72f6038a-7bc5-4b8e-865f-754f191bdd8e	063cf97b-0c40-4c82-a4f7-4f711ea11578	장거리 지구력주: 0.1km, 7:30min/km, 목적: 기본 유산소 능력 향상	t	2025-05-21 15:00:00+00
877546fe-5fce-475a-8999-75a03095a1f4	72f6038a-7bc5-4b8e-865f-754f191bdd8e	063cf97b-0c40-4c82-a4f7-4f711ea11578	회복주: 2km, 8:15min/km, 목적: 부담 완화 및 회복	f	2025-05-23 15:00:00+00
c8c1443c-388c-49c8-9d71-030a45fc9415	72f6038a-7bc5-4b8e-865f-754f191bdd8e	063cf97b-0c40-4c82-a4f7-4f711ea11578	인터벌: 0.4km x 3회(6:00min/km), 회복 0.4km 조깅, 목적: 고강도 구간 적응	f	2025-05-24 15:00:00+00
9f284888-053a-443b-9ef5-5003a37e0119	72f6038a-7bc5-4b8e-865f-754f191bdd8e	063cf97b-0c40-4c82-a4f7-4f711ea11578	파틀렉: 3km(1km예열 + 5x100m 스퍼트 + 1.5km 조깅), 7:50min/km, 목적: 페이스 전환 자극	f	2025-05-25 15:00:00+00
5646411f-5ff7-4298-97b0-200725d2221e	72f6038a-7bc5-4b8e-865f-754f191bdd8e	063cf97b-0c40-4c82-a4f7-4f711ea11578	휴식일: 목적: 피로 회복	\N	2025-05-26 15:00:00+00
693aa661-c1bf-4775-90b8-9fc686fc35f4	72f6038a-7bc5-4b8e-865f-754f191bdd8e	063cf97b-0c40-4c82-a4f7-4f711ea11578	회복주: 3km, 8:00min/km, 목적: 심폐 부담 경감	f	2025-05-27 15:00:00+00
0b31a23a-a79d-4cdc-8d25-d8c21e3222af	72f6038a-7bc5-4b8e-865f-754f191bdd8e	063cf97b-0c40-4c82-a4f7-4f711ea11578	장거리 지구력주: 5km, 7:30min/km, 목적: 심폐 지구력 향상	f	2025-05-28 15:00:00+00
637c2ad3-efe8-4912-b40c-3f6c94dc7dac	72f6038a-7bc5-4b8e-865f-754f191bdd8e	063cf97b-0c40-4c82-a4f7-4f711ea11578	회복주: 2.5km, 8:20min/km, 목적: 초보자 러닝 부상 예방	f	2025-05-29 15:00:00+00
f3c0dc07-7863-44c7-b61e-f7351a03d170	72f6038a-7bc5-4b8e-865f-754f191bdd8e	063cf97b-0c40-4c82-a4f7-4f711ea11578	중간 속도 지속주: 2km, 6:45min/km, 목적: 페이스 적응	f	2025-05-30 15:00:00+00
525e872f-f864-4428-bb01-57345a8ed3a8	72f6038a-7bc5-4b8e-865f-754f191bdd8e	063cf97b-0c40-4c82-a4f7-4f711ea11578	템포주: 3km(7:00min/km), 목적: 레이스페이스 적응	f	2025-05-31 15:00:00+00
7ba8d249-4687-475a-827c-3ef06b760c8a	72f6038a-7bc5-4b8e-865f-754f191bdd8e	063cf97b-0c40-4c82-a4f7-4f711ea11578	휴식일: 목적: 근육 회복	\N	2025-06-01 15:00:00+00
e17a315c-1d22-413b-a3cb-8dfbfebbc2bc	72f6038a-7bc5-4b8e-865f-754f191bdd8e	063cf97b-0c40-4c82-a4f7-4f711ea11578	회복주: 3km, 8:10min/km, 목적: 부상 위험 최소화	f	2025-06-02 15:00:00+00
57262bcb-820e-4286-b818-f9f98c63ccd5	72f6038a-7bc5-4b8e-865f-754f191bdd8e	063cf97b-0c40-4c82-a4f7-4f711ea11578	장거리 지구력주: 6km, 7:20min/km, 목적: 완주거리 적응	f	2025-06-03 15:00:00+00
b8d78997-3087-4f47-bb73-1c6221225923	72f6038a-7bc5-4b8e-865f-754f191bdd8e	063cf97b-0c40-4c82-a4f7-4f711ea11578	파틀렉: 4km(1km예열 + 6x100m 스퍼트 + 2km 조깅), 7:50min/km, 목적: 다양한 구간 대응력	f	2025-06-04 15:00:00+00
64531f69-5d70-4435-9b4b-aecd624a8c82	98fdce08-75cd-4837-a420-bc51bcc90e0e	a243b676-df47-42c3-a6cb-b91d60410ffd	0.2km 걷기와 가벼운 조깅을 반복하며 러닝에 몸을 적응시키세요.	f	2025-05-21 15:00:00+00
071ed48b-95d0-4155-ba0b-c48e3c755bc6	98fdce08-75cd-4837-a420-bc51bcc90e0e	a243b676-df47-42c3-a6cb-b91d60410ffd	휴식하여 전날 달린 후 회복하세요.	\N	2025-05-22 15:00:00+00
8feb7211-7ced-4e46-8c8e-7c693747ff99	98fdce08-75cd-4837-a420-bc51bcc90e0e	a243b676-df47-42c3-a6cb-b91d60410ffd	0.3km 짧은 산책 또는 가벼운 조깅으로 체력을 다지세요.	f	2025-05-23 15:00:00+00
51077767-2821-4dbe-9ed6-66a510c650a8	98fdce08-75cd-4837-a420-bc51bcc90e0e	a243b676-df47-42c3-a6cb-b91d60410ffd	휴식하며 스트레칭으로 유연성을 향상시키세요.	\N	2025-05-24 15:00:00+00
22909b58-874e-4831-b1a9-e2b0c3612e2e	98fdce08-75cd-4837-a420-bc51bcc90e0e	a243b676-df47-42c3-a6cb-b91d60410ffd	0.5km 천천히 조깅하며, 페이스를 꼭 유지하세요.	f	2025-05-25 15:00:00+00
a59e50fe-ad63-417c-9a2c-e9e6ab8f11be	98fdce08-75cd-4837-a420-bc51bcc90e0e	a243b676-df47-42c3-a6cb-b91d60410ffd	휴식하고 쉬는 날에는 충분히 걷거나 스트레칭하세요.	\N	2025-05-26 15:00:00+00
e77fd689-1041-4150-a636-cba2c6bbd263	98fdce08-75cd-4837-a420-bc51bcc90e0e	a243b676-df47-42c3-a6cb-b91d60410ffd	1km 부드럽게 달리며 호흡에 신경쓰세요.	f	2025-05-27 15:00:00+00
50406a5f-018a-475b-8091-db83dd217add	98fdce08-75cd-4837-a420-bc51bcc90e0e	a243b676-df47-42c3-a6cb-b91d60410ffd	휴식하며 몸의 피로도를 확인하세요.	\N	2025-05-28 15:00:00+00
2c2368c2-2371-4043-b46e-6d533c0aa554	98fdce08-75cd-4837-a420-bc51bcc90e0e	a243b676-df47-42c3-a6cb-b91d60410ffd	1.5km 천천히 달리기, 중간에 힘들면 걷기도 괜찮습니다.	f	2025-05-29 15:00:00+00
f40b90fa-3c6a-4e0e-ab8f-17eed2a033cb	98fdce08-75cd-4837-a420-bc51bcc90e0e	a243b676-df47-42c3-a6cb-b91d60410ffd	휴식하며 수분 섭취에 신경쓰세요.	\N	2025-05-30 15:00:00+00
e6f46250-cdd6-4d78-88ab-8b163ea0d283	98fdce08-75cd-4837-a420-bc51bcc90e0e	a243b676-df47-42c3-a6cb-b91d60410ffd	2km 일정한 속도로 달려보세요.	f	2025-05-31 15:00:00+00
8a8b1869-74d6-4b16-b68e-288809803949	98fdce08-75cd-4837-a420-bc51bcc90e0e	a243b676-df47-42c3-a6cb-b91d60410ffd	휴식을 취하세요.	\N	2025-06-01 15:00:00+00
0171840d-e949-4d77-9bb9-af5235600c8b	98fdce08-75cd-4837-a420-bc51bcc90e0e	a243b676-df47-42c3-a6cb-b91d60410ffd	2.5km 달리기, 중간에 지치면 가볍게 걸으면서 이어가세요.	f	2025-06-02 15:00:00+00
92cccc33-62c8-4262-b441-feb96fa25bed	98fdce08-75cd-4837-a420-bc51bcc90e0e	a243b676-df47-42c3-a6cb-b91d60410ffd	휴식하며 근육 풀기 마사지를 해보세요.	\N	2025-06-03 15:00:00+00
9866c5c6-3cf7-42bd-bb83-9d30a78a197c	98fdce08-75cd-4837-a420-bc51bcc90e0e	a243b676-df47-42c3-a6cb-b91d60410ffd	3km 천천히 달리고 마지막 200m는 힘을 내서 달려보세요.	f	2025-06-04 15:00:00+00
cc890eca-c7cf-49a6-b59c-8b57a99f55f0	98fdce08-75cd-4837-a420-bc51bcc90e0e	a243b676-df47-42c3-a6cb-b91d60410ffd	휴식하며 충분히 수면을 취하세요.	\N	2025-06-05 15:00:00+00
2e18254d-1112-4767-ac11-f7385162db80	98fdce08-75cd-4837-a420-bc51bcc90e0e	a243b676-df47-42c3-a6cb-b91d60410ffd	3.5km 달리기, 일정 페이스를 유지하는 연습을 하세요.	f	2025-06-06 15:00:00+00
ea7ba590-11fd-483f-9605-c4abdf77dc77	98fdce08-75cd-4837-a420-bc51bcc90e0e	a243b676-df47-42c3-a6cb-b91d60410ffd	휴식하거나, 가볍게 0.5km 걷기를 해도 좋습니다.	\N	2025-06-07 15:00:00+00
63495faa-addb-42d5-8aad-626ece204ef4	98fdce08-75cd-4837-a420-bc51bcc90e0e	a243b676-df47-42c3-a6cb-b91d60410ffd	4km 달리기, 2km마다 속도를 체크해 보세요.	f	2025-06-08 15:00:00+00
8034cfa7-1966-4bd4-b36f-66f6bc94d483	98fdce08-75cd-4837-a420-bc51bcc90e0e	a243b676-df47-42c3-a6cb-b91d60410ffd	휴식해서 에너지를 재충전하세요.	\N	2025-06-09 15:00:00+00
2d580995-2f6c-467b-aa78-617e19b05333	98fdce08-75cd-4837-a420-bc51bcc90e0e	a243b676-df47-42c3-a6cb-b91d60410ffd	4.5km 달리기, 물을 잘 챙기고 달리는 동안 호흡을 꾸준히 해보세요.	f	2025-06-10 15:00:00+00
ea51e4a3-68f6-4366-badd-66b6819bde94	98fdce08-75cd-4837-a420-bc51bcc90e0e	a243b676-df47-42c3-a6cb-b91d60410ffd	휴식하고 근력운동(스쿼트, 런지 등)을 가볍게 시도해보세요.	\N	2025-06-11 15:00:00+00
eae8ab53-cb34-4ac9-af65-4f47d849995f	98fdce08-75cd-4837-a420-bc51bcc90e0e	a243b676-df47-42c3-a6cb-b91d60410ffd	5km 도전, 너무 빠르지 않게 페이스 유지에 중점두세요.	f	2025-06-12 15:00:00+00
a35f084b-9b7e-4255-9ca9-25e2264c7fb1	98fdce08-75cd-4837-a420-bc51bcc90e0e	a243b676-df47-42c3-a6cb-b91d60410ffd	휴식하며 영양 섭취에 신경을 써주세요.	\N	2025-06-13 15:00:00+00
6272e1da-18a6-4f6d-aefa-c85709ab6611	98fdce08-75cd-4837-a420-bc51bcc90e0e	a243b676-df47-42c3-a6cb-b91d60410ffd	6km 달리기, 목표 거리의 절반을 달렸다는 점을 기억하세요.	f	2025-06-14 15:00:00+00
3c0298cb-9220-413b-9657-ec10592ca167	98fdce08-75cd-4837-a420-bc51bcc90e0e	a243b676-df47-42c3-a6cb-b91d60410ffd	휴식하며, 근육 통증은 냉찜질로 관리하세요.	\N	2025-06-15 15:00:00+00
427b55ae-8bc4-45f8-b24a-90b890351642	98fdce08-75cd-4837-a420-bc51bcc90e0e	a243b676-df47-42c3-a6cb-b91d60410ffd	7km 일정한 속도로 달리고, 적절히 걷기로 리듬을 조절하세요.	f	2025-06-16 15:00:00+00
000240d3-e638-4237-8a41-22848c133506	98fdce08-75cd-4837-a420-bc51bcc90e0e	a243b676-df47-42c3-a6cb-b91d60410ffd	휴식하며 가벼운 스트레칭을 해주세요.	\N	2025-06-17 15:00:00+00
bc38af93-ce23-4564-b469-150e497a0f8f	98fdce08-75cd-4837-a420-bc51bcc90e0e	a243b676-df47-42c3-a6cb-b91d60410ffd	8km 천천히 페이스 조절하며 장거리 달리기는 느긋하게 임하세요.	f	2025-06-18 15:00:00+00
0200c7ec-6637-450a-a15a-62b2f0e3aeb7	98fdce08-75cd-4837-a420-bc51bcc90e0e	a243b676-df47-42c3-a6cb-b91d60410ffd	휴식과 충분한 수면, 가벼운 산책으로 컨디션을 끌어올리세요.	\N	2025-06-19 15:00:00+00
8fecba50-6380-4de2-a840-7a42c7869043	98fdce08-75cd-4837-a420-bc51bcc90e0e	a243b676-df47-42c3-a6cb-b91d60410ffd	11.8km 도전, 차분히 처음부터 일정한 페이스로 목표 완주를 해보세요.	f	2025-06-20 15:00:00+00
aafab552-f776-41cd-b479-9d1de61ff85a	2ec8e248-4fb3-475a-9573-fb5c52551222	063cf97b-0c40-4c82-a4f7-4f711ea11578	회복주: 0.1km, 8:00min/km, 목적: 근육 적응 및 회복	f	2025-05-20 15:00:00+00
74b5d3be-85be-4388-b7ba-6ca613b1c090	5c7d7e3b-9115-4a5a-aa04-80742fbbc693	1a27f815-0c6c-4bf1-b2c9-9cd88dd9ea44	회복주: 5km, 8:00min/km, 목적: 근육 회복/훈련 적응	f	2025-05-21 15:00:00+00
71bfdce4-8cab-4d90-9b4d-c118f0ef7f60	5c7d7e3b-9115-4a5a-aa04-80742fbbc693	1a27f815-0c6c-4bf1-b2c9-9cd88dd9ea44	중간 속도 지속주: 6km, 4:00min/km, Zone3, 목적: 중간 강도 지구력 강화	f	2025-05-22 15:00:00+00
3483c1b3-084f-42ab-baa7-3ab216e627c7	5c7d7e3b-9115-4a5a-aa04-80742fbbc693	1a27f815-0c6c-4bf1-b2c9-9cd88dd9ea44	인터벌: 0.8km x 5회, 레이스페이스, 회복 400m, 목적: 속도/스피드 유지 능력 향상	f	2025-05-23 15:00:00+00
dbbbe5de-4960-496c-94aa-a030e31d7bc2	5c7d7e3b-9115-4a5a-aa04-80742fbbc693	1a27f815-0c6c-4bf1-b2c9-9cd88dd9ea44	휴식일: 목적: 체력/에너지 회복	\N	2025-05-24 15:00:00+00
0efca103-88e1-439c-ad3d-b56b7496d93f	5c7d7e3b-9115-4a5a-aa04-80742fbbc693	1a27f815-0c6c-4bf1-b2c9-9cd88dd9ea44	장거리 지구력주: 10km, 6:00min/km, Zone2, 목적: 완주 지구력 강화	f	2025-05-25 15:00:00+00
148cced1-d84d-415d-bc98-9a1a9fdbd4a7	5c7d7e3b-9115-4a5a-aa04-80742fbbc693	1a27f815-0c6c-4bf1-b2c9-9cd88dd9ea44	파틀렉: 8km, 조깅 + 100m x 10회 언덕 스퍼트 포함, 목적: 변속 주력/근지구력 강화	f	2025-05-26 15:00:00+00
88206295-4b8c-4580-8723-c389c31aa6be	5c7d7e3b-9115-4a5a-aa04-80742fbbc693	1a27f815-0c6c-4bf1-b2c9-9cd88dd9ea44	회복주: 5km, 8:00min/km, 목적: 근육 회복/피로 관리	f	2025-05-27 15:00:00+00
46ea886c-05f5-4f1d-a90b-81c400c55810	5c7d7e3b-9115-4a5a-aa04-80742fbbc693	1a27f815-0c6c-4bf1-b2c9-9cd88dd9ea44	중간 속도 지속주: 5km, 4:00min/km, Zone3, 목적: 중강도 적응/속도 전환 준비	f	2025-05-28 15:00:00+00
e7380c48-1caa-4b33-8b32-10ff4d4eacb6	5c7d7e3b-9115-4a5a-aa04-80742fbbc693	1a27f815-0c6c-4bf1-b2c9-9cd88dd9ea44	템포주: 6km(2km 이지, 3km 템포 4:25min/km, 1km 이지), 목적: 레이스페이스 감각 적응	f	2025-05-29 15:00:00+00
6496af3c-7d65-424c-b5a5-a0621b912bd3	5c7d7e3b-9115-4a5a-aa04-80742fbbc693	1a27f815-0c6c-4bf1-b2c9-9cd88dd9ea44	휴식일: 목적: 체력/에너지 회복	\N	2025-05-30 15:00:00+00
e40d9969-4582-4a85-8247-4fb07609f686	5c7d7e3b-9115-4a5a-aa04-80742fbbc693	1a27f815-0c6c-4bf1-b2c9-9cd88dd9ea44	장거리 지구력주: 10km, 6:00min/km, Zone2, 목적: 완주 지구력 유지	f	2025-05-31 15:00:00+00
0beeb796-2094-42f5-8d8b-e71270c4da4d	5c7d7e3b-9115-4a5a-aa04-80742fbbc693	1a27f815-0c6c-4bf1-b2c9-9cd88dd9ea44	회복주: 4km, 8:10min/km, 목적: 피로 해소 및 근육 회복	f	2025-06-01 15:00:00+00
7d23d77b-e68b-4d88-87df-9c0303e293f5	5c7d7e3b-9115-4a5a-aa04-80742fbbc693	1a27f815-0c6c-4bf1-b2c9-9cd88dd9ea44	파틀렉: 7km, 200m x 8회 스퍼트+조깅 반복, 목적: 근지구력/심폐 향상	f	2025-06-02 15:00:00+00
96bc6424-ae7e-4e69-a3f4-35c632336551	5c7d7e3b-9115-4a5a-aa04-80742fbbc693	1a27f815-0c6c-4bf1-b2c9-9cd88dd9ea44	인터벌: 0.4km x 6회, 레이스페이스, 회복 400m, 목적: 레이스 속도 감각	f	2025-06-03 15:00:00+00
c850ec18-8fbe-427f-80f1-409beed8b88d	5c7d7e3b-9115-4a5a-aa04-80742fbbc693	1a27f815-0c6c-4bf1-b2c9-9cd88dd9ea44	회복주: 3km, 8:30min/km, 목적: 컨디션 최적화	f	2025-06-04 15:00:00+00
4f930af2-1312-40d8-8324-dfca344ec00f	5c7d7e3b-9115-4a5a-aa04-80742fbbc693	1a27f815-0c6c-4bf1-b2c9-9cd88dd9ea44	휴식일: 목적: 피로 완전 회복 및 최적의 신체상태 준비	\N	2025-06-05 15:00:00+00
7c33b8ba-71a0-442a-9383-07adcb2cc9c1	5c7d7e3b-9115-4a5a-aa04-80742fbbc693	1a27f815-0c6c-4bf1-b2c9-9cd88dd9ea44	10km 마라톤 레이스: 레이스페이스 (목표 페이스), 목적: 목표 기록 달성	\N	2025-06-06 15:00:00+00
cd9994ac-abe8-4e97-97f7-45d408f92a6c	4ae429ee-ff90-4e21-8b81-586e94ff3a33	962c3730-2d81-43fb-a4ac-30e2196a81c3	5km 페이스 조절에 집중하며 달리기, 무리하지 말고 워밍업과 쿨다운 포함하기	f	2025-05-21 15:00:00+00
3e61a420-0477-4b71-a206-a3c847924541	4ae429ee-ff90-4e21-8b81-586e94ff3a33	962c3730-2d81-43fb-a4ac-30e2196a81c3	3km 가볍게 조깅하며 컨디션 점검 및 스트레칭 철저히 하기	f	2025-05-22 15:00:00+00
c7610ea8-6e45-4ae7-af69-df5bdd5330d2	4ae429ee-ff90-4e21-8b81-586e94ff3a33	962c3730-2d81-43fb-a4ac-30e2196a81c3	10km 에너지를 아끼며 레이스 진행, 평소 페이스 유지하며 완주하기	f	2025-05-23 15:00:00+00
d89fb544-0487-4174-b5d5-6fc68da381b5	00d3da65-f7d0-4285-978a-42ce538d1739	dfeff5ca-d525-4062-892a-f56b3844f724	중간 속도 지속주: 6km, Zone3, 목적: 중간 강도 스피드 내구성 향상	f	2025-05-21 15:00:00+00
64978dc6-cc13-47d3-8a94-f2dc3c883465	00d3da65-f7d0-4285-978a-42ce538d1739	dfeff5ca-d525-4062-892a-f56b3844f724	회복주: 5km, Zone1, 목적: 피로 회복 및 컨디션 유지	f	2025-05-22 15:00:00+00
ca85b1f3-9be9-4747-8f1e-08fcc2b0d3d1	00d3da65-f7d0-4285-978a-42ce538d1739	dfeff5ca-d525-4062-892a-f56b3844f724	인터벌: 0.8km x 4회, 레이스페이스, 목적: 속도 및 ST 에너지 시스템 강화	f	2025-05-23 15:00:00+00
1cb8812b-a6f4-4c32-85eb-8a3927c1068c	00d3da65-f7d0-4285-978a-42ce538d1739	dfeff5ca-d525-4062-892a-f56b3844f724	휴식일: 목적: 근육 회복 및 피로 관리	\N	2025-05-24 15:00:00+00
32b326c2-5817-4c33-a1c2-7270636a92c7	00d3da65-f7d0-4285-978a-42ce538d1739	dfeff5ca-d525-4062-892a-f56b3844f724	장거리 지구력주: 10km, Zone2, 목적: 유산소 지구력 증가 및 체력 확보	f	2025-05-25 15:00:00+00
f206e32d-cae5-40ab-98b8-dafe1bc9ff11	00d3da65-f7d0-4285-978a-42ce538d1739	dfeff5ca-d525-4062-892a-f56b3844f724	회복주: 4km, Zone1, 목적: 피로 회복	f	2025-05-26 15:00:00+00
504a580f-e62e-40f4-b65d-89da17d32083	00d3da65-f7d0-4285-978a-42ce538d1739	dfeff5ca-d525-4062-892a-f56b3844f724	템포주: 5km, 레이스페이스 ±10%, 목적: 레이스 감각 습득 및 LT 한계 상향	f	2025-05-27 15:00:00+00
7cd9e51b-a141-48ee-a2ce-c61fbcddc16b	00d3da65-f7d0-4285-978a-42ce538d1739	dfeff5ca-d525-4062-892a-f56b3844f724	파틀렉: 7km, 예열 2km, 200m 스퍼트 x 6회 포함, 목적: 변속 훈련 및 파워 향상	f	2025-05-28 15:00:00+00
61e33021-16f4-4f2a-993a-721b5f0258de	00d3da65-f7d0-4285-978a-42ce538d1739	dfeff5ca-d525-4062-892a-f56b3844f724	회복주: 5km, Zone1, 목적: 피로 회복	f	2025-05-29 15:00:00+00
f5513a6f-b52a-4b6b-8b3d-bed80b328535	00d3da65-f7d0-4285-978a-42ce538d1739	dfeff5ca-d525-4062-892a-f56b3844f724	중간 속도 지속주: 7km, Zone3, 목적: 내구성 및 스피드 유지	f	2025-05-30 15:00:00+00
1860bcfd-7951-47bf-858b-9ec107c401ee	00d3da65-f7d0-4285-978a-42ce538d1739	dfeff5ca-d525-4062-892a-f56b3844f724	휴식일: 목적: 근육 회복	\N	2025-05-31 15:00:00+00
50e270c2-00ec-4024-b98d-365ff0cc51ad	00d3da65-f7d0-4285-978a-42ce538d1739	dfeff5ca-d525-4062-892a-f56b3844f724	장거리 지구력주: 12km, Zone2, 목적: 장거리 유산소 적응	f	2025-06-01 15:00:00+00
220c28c7-1fd5-4e69-a5a2-fcc455632425	00d3da65-f7d0-4285-978a-42ce538d1739	dfeff5ca-d525-4062-892a-f56b3844f724	회복주: 5km, Zone1, 목적: 회복과 부상 예방	f	2025-06-02 15:00:00+00
4d4dc7fc-6c43-4610-880e-45fbdac5d96f	00d3da65-f7d0-4285-978a-42ce538d1739	dfeff5ca-d525-4062-892a-f56b3844f724	템포주: 6km, 레이스페이스 ±10%, 목적: 위 임계치 훈련 및 페이스 감각 강화	f	2025-06-03 15:00:00+00
c2bbd07b-e221-4241-8e46-c64a8ab0538c	00d3da65-f7d0-4285-978a-42ce538d1739	dfeff5ca-d525-4062-892a-f56b3844f724	인터벌: 0.8km x 5회, 레이스페이스, 400m 회복, 목적: 속도 내구성 및 VO2max 증진	f	2025-06-04 15:00:00+00
996c954d-47e6-45d3-9e5d-da12785d8554	00d3da65-f7d0-4285-978a-42ce538d1739	dfeff5ca-d525-4062-892a-f56b3844f724	파틀렉: 8km, 100~200m 스퍼트/조깅 반복, 목적: 변속 적응 및 근파워 증가	f	2025-06-05 15:00:00+00
48fe12f8-8ce7-43fd-ac94-8b003d94d6f1	00d3da65-f7d0-4285-978a-42ce538d1739	dfeff5ca-d525-4062-892a-f56b3844f724	회복주: 5km, Zone1, 목적: 피로 회복	f	2025-06-06 15:00:00+00
8597ce33-462e-4aac-813d-bf06a1e00d99	00d3da65-f7d0-4285-978a-42ce538d1739	dfeff5ca-d525-4062-892a-f56b3844f724	휴식일: 목적: 체력 회복	\N	2025-06-07 15:00:00+00
7c10f93e-d678-4ab4-9985-f6967e56b7c2	00d3da65-f7d0-4285-978a-42ce538d1739	dfeff5ca-d525-4062-892a-f56b3844f724	중간 속도 지속주: 7km, Zone3, 목적: 중간 강도 스피드 내구성 강화	f	2025-06-08 15:00:00+00
13c71e0c-9a83-48d1-99e0-b69f963abd53	72f6038a-7bc5-4b8e-865f-754f191bdd8e	063cf97b-0c40-4c82-a4f7-4f711ea11578	회복주: 2.5km, 8:15min/km, 목적: 피로 부담 해소	f	2025-06-05 15:00:00+00
f557e452-f13d-426c-a449-79c06a1bdfc4	72f6038a-7bc5-4b8e-865f-754f191bdd8e	063cf97b-0c40-4c82-a4f7-4f711ea11578	인터벌: 0.6km x 4회(6:30min/km), 회복 0.4km 조깅, 목적: 체력 및 스피드 혼합 자극	f	2025-06-06 15:00:00+00
5cb4cf17-5877-4f25-a202-ffc034c4176f	72f6038a-7bc5-4b8e-865f-754f191bdd8e	063cf97b-0c40-4c82-a4f7-4f711ea11578	휴식일: 목적: 회복 및 준비	\N	2025-06-07 15:00:00+00
65edb034-fe55-44dd-b3e9-b231b78f874c	72f6038a-7bc5-4b8e-865f-754f191bdd8e	063cf97b-0c40-4c82-a4f7-4f711ea11578	회복주: 3.5km, 8:10min/km, 목적: 피로 누적 방지	f	2025-06-08 15:00:00+00
dee3c9fc-82d7-47c9-855a-baef678437b6	72f6038a-7bc5-4b8e-865f-754f191bdd8e	063cf97b-0c40-4c82-a4f7-4f711ea11578	장거리 지구력주: 7km, 7:15min/km, 목적: 10km 완주 체력 완성	f	2025-06-09 15:00:00+00
ce30fc99-da98-485e-8be6-cfc698d82186	72f6038a-7bc5-4b8e-865f-754f191bdd8e	063cf97b-0c40-4c82-a4f7-4f711ea11578	파틀렉: 5km(1km예열 + 8x100m 스퍼트 + 2.2km 조깅), 7:40min/km, 목적: 페이스 유동성	f	2025-06-10 15:00:00+00
9e799158-517f-4cac-80fa-49c2eff9add5	72f6038a-7bc5-4b8e-865f-754f191bdd8e	063cf97b-0c40-4c82-a4f7-4f711ea11578	회복주: 3km, 8:20min/km, 목적: 근육 피로 회복	f	2025-06-11 15:00:00+00
3897bbfc-a10c-4584-8fd4-497845248eec	72f6038a-7bc5-4b8e-865f-754f191bdd8e	063cf97b-0c40-4c82-a4f7-4f711ea11578	중간 속도 지속주: 3km, 6:45min/km, 목적: 10km 체력 정밀 조정	f	2025-06-12 15:00:00+00
8bed48f5-cb14-448f-b38d-d71d0cf0aae7	72f6038a-7bc5-4b8e-865f-754f191bdd8e	063cf97b-0c40-4c82-a4f7-4f711ea11578	회복주: 2km, 8:30min/km, 목적: 피로 완화 및 컨디션 설정	f	2025-06-13 15:00:00+00
895d0b13-26f7-4d97-bcb5-6e6794947697	72f6038a-7bc5-4b8e-865f-754f191bdd8e	063cf97b-0c40-4c82-a4f7-4f711ea11578	10km 마라톤 레이스: 10km, 6:45–7:00min/km, 목적: 목표 레이스 완주	f	2025-06-14 15:00:00+00
571467b8-bcf0-4466-b557-2cb967b773ec	00d3da65-f7d0-4285-978a-42ce538d1739	dfeff5ca-d525-4062-892a-f56b3844f724	회복주: 4km, Zone1, 목적: 근육 회복	f	2025-06-09 15:00:00+00
74d9c121-1c48-4de0-a114-0d7fbad84848	00d3da65-f7d0-4285-978a-42ce538d1739	dfeff5ca-d525-4062-892a-f56b3844f724	장거리 지구력주: 13km, Zone2, 목적: 완주 체력 만들기	f	2025-06-10 15:00:00+00
a864444c-4409-4af7-9157-778ac05cf536	00d3da65-f7d0-4285-978a-42ce538d1739	dfeff5ca-d525-4062-892a-f56b3844f724	템포주: 7km, 레이스페이스 ±10%, 목적: 레이스페이스 내구성 완성	f	2025-06-11 15:00:00+00
fb3f4534-7765-4dfa-9508-e9b1e612a214	00d3da65-f7d0-4285-978a-42ce538d1739	dfeff5ca-d525-4062-892a-f56b3844f724	인터벌: 400m x 8회, 레이스페이스, 400m 회복, 목적: 피크 컨디션 유도	f	2025-06-12 15:00:00+00
8eb57b95-0b72-496b-be8a-74cdab61b24d	00d3da65-f7d0-4285-978a-42ce538d1739	dfeff5ca-d525-4062-892a-f56b3844f724	회복주: 4km, Zone1, 목적: 컨디션 조정	f	2025-06-13 15:00:00+00
5dc64af2-8a0f-4c82-8414-fc6e7243cfe0	00d3da65-f7d0-4285-978a-42ce538d1739	dfeff5ca-d525-4062-892a-f56b3844f724	마라톤 대회: 15km, 목표 페이스, 목적: 레이스 완주 및 목표 달성	f	2025-06-14 15:00:00+00
472979f8-0e38-4b0d-b5c6-d57db3392306	11480fa0-d596-4f74-8b42-3120e3869484	0f9c3869-310c-48c5-b838-c50fa0ad5bc1	회복주: 5km, 7:30min/km, 목적: 주 훈련 전 회복 및 컨디션 점검	f	2025-05-21 15:00:00+00
f12850c5-bbf0-4d1a-8478-6d62be897f73	ca1ae130-c20e-48e7-b6d7-7fe8350f2b98	15b88134-e3c4-4822-9bfa-244c6a185913	휴식일: 목적: 신체 및 정신 회복	\N	2025-05-27 15:00:00+00
fc62a70f-7426-425b-8cc8-68bb85f6373c	ca1ae130-c20e-48e7-b6d7-7fe8350f2b98	15b88134-e3c4-4822-9bfa-244c6a185913	템포주: 6km, 4:50min/km, 목적: 지속가능 속도 향상	f	2025-05-28 15:00:00+00
640f53d3-0c3a-4ca1-a4b6-f58529c92992	ca1ae130-c20e-48e7-b6d7-7fe8350f2b98	15b88134-e3c4-4822-9bfa-244c6a185913	회복주: 5km, 8:10min/km, 목적: 심폐피로 완화	f	2025-05-29 15:00:00+00
b9d2f0ee-b57b-4942-bc7d-93c477f002e5	ca1ae130-c20e-48e7-b6d7-7fe8350f2b98	15b88134-e3c4-4822-9bfa-244c6a185913	중간 속도 지속주: 8km, 4:18min/km, 목적: 스피드 지속력 향상	f	2025-05-30 15:00:00+00
6a83690b-7082-4c2a-bd9f-b98d369c9dec	ca1ae130-c20e-48e7-b6d7-7fe8350f2b98	15b88134-e3c4-4822-9bfa-244c6a185913	인터벌: 0.4km x 6회 (3:50min/km), 회복주 0.4km x 6회, 목적: 운동강도 부하 적응	f	2025-05-31 15:00:00+00
8fb18cd5-1eb8-44a6-8f49-d20629596a54	ca1ae130-c20e-48e7-b6d7-7fe8350f2b98	15b88134-e3c4-4822-9bfa-244c6a185913	회복주: 4km, 8:00min/km, 목적: 피로 누적 방지	f	2025-06-01 15:00:00+00
20027663-de28-4a59-b40b-c98439b0e8a8	ca1ae130-c20e-48e7-b6d7-7fe8350f2b98	15b88134-e3c4-4822-9bfa-244c6a185913	파틀렉: 12km(스퍼트 150m+언덕 반복), Zone2~3, 목적: 다양한 자극 적용	f	2025-06-02 15:00:00+00
acf2efa2-4941-4efc-8c98-e25b8bc2c80b	ca1ae130-c20e-48e7-b6d7-7fe8350f2b98	15b88134-e3c4-4822-9bfa-244c6a185913	휴식일: 목적: 회복 및 컨디셔닝	\N	2025-06-03 15:00:00+00
ea6418e8-a360-4dfe-9207-a82013303039	ca1ae130-c20e-48e7-b6d7-7fe8350f2b98	15b88134-e3c4-4822-9bfa-244c6a185913	장거리 지구력주: 18km, 6:35min/km, 목적: 지구력이 요구되는 후반부 대비	f	2025-06-04 15:00:00+00
9ad2ea7c-c2ac-4e51-b12b-75fd1c4568f7	ca1ae130-c20e-48e7-b6d7-7fe8350f2b98	15b88134-e3c4-4822-9bfa-244c6a185913	회복주: 6km, 8:20min/km, 목적: 심폐 회복	f	2025-06-05 15:00:00+00
15b3f272-ec2b-482b-820e-49dd6c1f9296	34ea3a89-2625-4148-9879-6e39d41cc181	6ffb74df-9bc4-41ed-b2b8-2b949e07f0b2	회복주: 3km, 8:30min/km, 목적: 몸 상태 점검 및 부상 예방	f	2025-05-21 15:00:00+00
ec7a300d-00ab-487a-92e0-81d3d5729cf2	34ea3a89-2625-4148-9879-6e39d41cc181	6ffb74df-9bc4-41ed-b2b8-2b949e07f0b2	휴식일: 목적: 회복 및 근육 적응	\N	2025-05-22 15:00:00+00
936079d6-695c-4d1b-b8e8-69847711caba	34ea3a89-2625-4148-9879-6e39d41cc181	6ffb74df-9bc4-41ed-b2b8-2b949e07f0b2	장거리 지구력주: 5km, 7:30min/km, 목적: 유산소 지구력 향상 및 장거리 적응	f	2025-05-23 15:00:00+00
df9b61e0-6f77-404e-ab0d-a1a018eddec6	34ea3a89-2625-4148-9879-6e39d41cc181	6ffb74df-9bc4-41ed-b2b8-2b949e07f0b2	회복주: 2km, 8:30min/km, 목적: 피로 회복 및 부상 예방	f	2025-05-24 15:00:00+00
3869e6e8-ca5b-4cd2-b3e1-c5ac87376704	34ea3a89-2625-4148-9879-6e39d41cc181	6ffb74df-9bc4-41ed-b2b8-2b949e07f0b2	중간 속도 지속주: 2km, 6:30min/km, 목적: 유산소 파워, 풀 마라톤 러닝 자세 습득	f	2025-05-25 15:00:00+00
4ad61468-32c4-466c-9bf8-76cdbc8fd36a	34ea3a89-2625-4148-9879-6e39d41cc181	6ffb74df-9bc4-41ed-b2b8-2b949e07f0b2	템포주: 3km, 6:00min/km, 목적: 레이스페이스 적응 및 주요 근지구력 향상	f	2025-05-26 15:00:00+00
9dd63409-3ae5-4c4f-9a33-013ca3eb8de4	34ea3a89-2625-4148-9879-6e39d41cc181	6ffb74df-9bc4-41ed-b2b8-2b949e07f0b2	인터벌: 0.4km x 4회, 5:30min/km(인터벌), 휴식 0.4km 조깅(8:00min/km), 목적: 심폐지구력 및 스피드 향상	f	2025-05-27 15:00:00+00
43e7e806-a3e1-4882-9919-f6064ce29bc4	34ea3a89-2625-4148-9879-6e39d41cc181	6ffb74df-9bc4-41ed-b2b8-2b949e07f0b2	파틀렉: 4km, 구간별 0.1km(빠르게) + 0.4km(조깅) 반복, 목적: 변속 주행 적응 및 근파워 향상	f	2025-05-28 15:00:00+00
c617339e-6848-468e-a401-114945619dd5	34ea3a89-2625-4148-9879-6e39d41cc181	6ffb74df-9bc4-41ed-b2b8-2b949e07f0b2	회복주: 2km, 8:30min/km, 목적: 대회 전 컨디션 회복 및 부상 위험 최소화	f	2025-05-29 15:00:00+00
65e9a2e6-19c1-45a0-876d-53f183605db3	34ea3a89-2625-4148-9879-6e39d41cc181	6ffb74df-9bc4-41ed-b2b8-2b949e07f0b2	10km 레이스: 10km, 목표페이스 최대 유지(6:30–6:00min/km), 목적: 마라톤 목표 달성 및 페이스 분배 실전 적용	f	2025-05-30 15:00:00+00
5390ad6a-1ff5-4dcb-96b5-589a9aebde68	21767762-edec-4078-9f7b-21f26dee68d4	0f9c3869-310c-48c5-b838-c50fa0ad5bc1	회복주: 5km, 7:45min/km, 목적: 적응 및 부상 방지	f	2025-05-21 15:00:00+00
84fbfab5-a437-40ce-b39a-f9fcf53ef492	21767762-edec-4078-9f7b-21f26dee68d4	0f9c3869-310c-48c5-b838-c50fa0ad5bc1	휴식일: 목적: 피로회복 및 에너지 축적	\N	2025-05-22 15:00:00+00
8b6a9d60-2029-4037-9af2-a38e23c2a985	21767762-edec-4078-9f7b-21f26dee68d4	0f9c3869-310c-48c5-b838-c50fa0ad5bc1	마라톤 레이스: 25km, 목표 페이스 6:40min/km, 목적: 레이스 완주 및 경험 획득	f	2025-05-23 15:00:00+00
e86f1605-81c7-4302-85ea-88587f62acbb	ca1ae130-c20e-48e7-b6d7-7fe8350f2b98	15b88134-e3c4-4822-9bfa-244c6a185913	중간 속도 지속주: 6km, 4:20min/km, 목적: 유산소 파워 및 레이스페이스 적응	f	2025-05-21 15:00:00+00
f6ce9593-2cae-4f69-9200-b700868b02e1	ca1ae130-c20e-48e7-b6d7-7fe8350f2b98	15b88134-e3c4-4822-9bfa-244c6a185913	회복주: 5km, 7:50min/km, 목적: 체력회복 및 부상예방	f	2025-05-22 15:00:00+00
3f685932-052d-4f23-82cb-1b01c84ede67	ca1ae130-c20e-48e7-b6d7-7fe8350f2b98	15b88134-e3c4-4822-9bfa-244c6a185913	인터벌: 0.8km x 4회 (4:00min/km), 회복주 0.4km x 4회, 목적: LT 향상 및 순발력 자극	f	2025-05-23 15:00:00+00
0a9a60e5-3d8c-40c4-a0f1-afc2ff0c5b98	ca1ae130-c20e-48e7-b6d7-7fe8350f2b98	15b88134-e3c4-4822-9bfa-244c6a185913	파틀렉: 10km(스퍼트 200m+조깅 반복), Zone2~3, 목적: 변화 주기 적응	f	2025-05-24 15:00:00+00
22c02ccd-912e-4207-a5db-097362787851	ca1ae130-c20e-48e7-b6d7-7fe8350f2b98	15b88134-e3c4-4822-9bfa-244c6a185913	회복주: 4km, 8:00min/km, 목적: 근육회복 및 가볍게 순환	f	2025-05-25 15:00:00+00
07887b5a-7ef1-45b1-aa95-606a50cec5d5	ca1ae130-c20e-48e7-b6d7-7fe8350f2b98	15b88134-e3c4-4822-9bfa-244c6a185913	장거리 지구력주: 14km, 6:45min/km, 목적: 지구력 증가	f	2025-05-26 15:00:00+00
45945ce8-9f88-40e0-a433-bba1a500e3bd	ca1ae130-c20e-48e7-b6d7-7fe8350f2b98	15b88134-e3c4-4822-9bfa-244c6a185913	템포주: 8km, 4:45min/km, 목적: 레이스페이스의 효율성 강화	f	2025-06-06 15:00:00+00
763395f6-b8fe-4ced-95e0-44e2688a039c	ca1ae130-c20e-48e7-b6d7-7fe8350f2b98	15b88134-e3c4-4822-9bfa-244c6a185913	회복주: 4km, 7:55min/km, 목적: 근육 피로 최소화	f	2025-06-07 15:00:00+00
fba8193f-6f95-427e-889b-27e3dd333c86	ca1ae130-c20e-48e7-b6d7-7fe8350f2b98	15b88134-e3c4-4822-9bfa-244c6a185913	중간 속도 지속주: 10km, 4:15min/km, 목적: 중간 페이스 스태미나 향상	f	2025-06-08 15:00:00+00
7b9577ac-556a-4724-ad89-8ae863347249	ca1ae130-c20e-48e7-b6d7-7fe8350f2b98	15b88134-e3c4-4822-9bfa-244c6a185913	인터벌: 1km x 3회 (4:05min/km), 회복주 0.5km x 3회, 목적: 스피드 근지구력 강화	f	2025-06-09 15:00:00+00
351ee27c-92a9-4a0b-8c84-042bacfe9baf	ca1ae130-c20e-48e7-b6d7-7fe8350f2b98	15b88134-e3c4-4822-9bfa-244c6a185913	파틀렉: 14km(스퍼트 200m+조깅 반복), Zone2~3, 목적: 변화주 내성	f	2025-06-10 15:00:00+00
b40eb00a-a549-460f-b9a4-ec6717bc90ed	ca1ae130-c20e-48e7-b6d7-7fe8350f2b98	15b88134-e3c4-4822-9bfa-244c6a185913	회복주: 5km, 8:00min/km, 목적: 컨디션 유지	f	2025-06-11 15:00:00+00
a3afe397-a143-4153-9fe7-fb337ebdce04	ca1ae130-c20e-48e7-b6d7-7fe8350f2b98	15b88134-e3c4-4822-9bfa-244c6a185913	장거리 지구력주: 22km, 6:30min/km, 목적: 마라톤 체력 베이스 상승	f	2025-06-12 15:00:00+00
81034aa3-b8e8-40da-8819-d9b12aec6342	ca1ae130-c20e-48e7-b6d7-7fe8350f2b98	15b88134-e3c4-4822-9bfa-244c6a185913	휴식일: 목적: 근육 회복 및 컨디션 점검	\N	2025-06-13 15:00:00+00
0b7beb36-6fb9-4a6b-8b39-baa02f76e7fb	ca1ae130-c20e-48e7-b6d7-7fe8350f2b98	15b88134-e3c4-4822-9bfa-244c6a185913	템포주: 10km, 4:50min/km, 목적: 지속 페이스 완성	f	2025-06-14 15:00:00+00
504273a9-29d4-44e8-883a-babe0469af54	ca1ae130-c20e-48e7-b6d7-7fe8350f2b98	15b88134-e3c4-4822-9bfa-244c6a185913	회복주: 5km, 8:10min/km, 목적: 실제 레이스 전 피로 해소	f	2025-06-15 15:00:00+00
58b9850d-f7ae-4ec2-8109-cb3a16280136	ca1ae130-c20e-48e7-b6d7-7fe8350f2b98	15b88134-e3c4-4822-9bfa-244c6a185913	중간 속도 지속주: 8km, 4:10min/km, 목적: 폼 회복 및 페이스 조율	f	2025-06-16 15:00:00+00
489bec35-878a-4e07-8bf9-2e76c52effd6	ca1ae130-c20e-48e7-b6d7-7fe8350f2b98	15b88134-e3c4-4822-9bfa-244c6a185913	파틀렉: 10km(스퍼트 100m+조깅 반복), Zone2, 목적: 기민성/리듬감 생기 부여	f	2025-06-17 15:00:00+00
07b3e9cd-f4e4-4554-8d08-abded903cbd9	ca1ae130-c20e-48e7-b6d7-7fe8350f2b98	15b88134-e3c4-4822-9bfa-244c6a185913	회복주: 4km, 8:20min/km, 목적: 피로최소화 집중	f	2025-06-18 15:00:00+00
086ed145-2af7-44ac-9407-ea68b00a7d00	ca1ae130-c20e-48e7-b6d7-7fe8350f2b98	15b88134-e3c4-4822-9bfa-244c6a185913	인터벌: 0.8km x 2회 (4:00min/km), 회복주 0.4km x 2회, 목적: 가벼운 스피드자극	f	2025-06-19 15:00:00+00
ad6b76c6-dbd0-4b4b-8d78-9acc58d2ad49	ca1ae130-c20e-48e7-b6d7-7fe8350f2b98	15b88134-e3c4-4822-9bfa-244c6a185913	회복주: 3km, 8:30min/km, 목적: 가볍게 몸풀기	f	2025-06-20 15:00:00+00
d1b69c9a-71e7-4436-adaf-8f77430aec06	ca1ae130-c20e-48e7-b6d7-7fe8350f2b98	15b88134-e3c4-4822-9bfa-244c6a185913	마라톤 이벤트: 1031km, 완주 목표, 목적: 체력과 의지의 종합 검증	f	2025-06-21 15:00:00+00
d0a60703-c7db-4e67-8644-f540a3a6646a	2ff5653d-7a86-499f-9ffd-943b1b1b4a78	93db6be8-c5bb-4bf5-ac84-11fcf9dae304	회복주: 2km, 8:30min/km, 목적: 주행 습관 형성 및 근육 적응	f	2025-05-21 15:00:00+00
c5bc33a4-f0d0-46ca-97b1-81131f25d934	2ff5653d-7a86-499f-9ffd-943b1b1b4a78	93db6be8-c5bb-4bf5-ac84-11fcf9dae304	휴식일: 목적: 신체 회복 및 피로 방지	\N	2025-05-22 15:00:00+00
c44c7bf4-f441-438b-9208-da0272a005fa	2ff5653d-7a86-499f-9ffd-943b1b1b4a78	93db6be8-c5bb-4bf5-ac84-11fcf9dae304	중간 속도 지속주: 1.5km, 7:00min/km, 목적: 속도감각 및 주행 효율 증가	f	2025-05-23 15:00:00+00
885b394c-3b51-49fa-93ae-b615818c8670	2ff5653d-7a86-499f-9ffd-943b1b1b4a78	93db6be8-c5bb-4bf5-ac84-11fcf9dae304	회복주: 2km, 8:15min/km, 목적: 근육 피로 회복	f	2025-05-24 15:00:00+00
b8c5a7ec-7b15-41da-8091-3a84cc9abcf4	2ff5653d-7a86-499f-9ffd-943b1b1b4a78	93db6be8-c5bb-4bf5-ac84-11fcf9dae304	장거리 지구력주: 3km, 7:45min/km, 목적: 지구력 기초 형성	f	2025-05-25 15:00:00+00
7285fc2c-9e93-4f38-bae6-af8f78ab6b6a	2ff5653d-7a86-499f-9ffd-943b1b1b4a78	93db6be8-c5bb-4bf5-ac84-11fcf9dae304	인터벌: 0.4km x 3회, 6:00min/km 스퍼트, 0.4km 회복조깅, 목적: 속도 및 심폐기능 자극	f	2025-05-26 15:00:00+00
90be462f-da99-4d41-b502-94db59b162bd	2ff5653d-7a86-499f-9ffd-943b1b1b4a78	93db6be8-c5bb-4bf5-ac84-11fcf9dae304	휴식일: 목적: 근육 재생 및 부상 예방	\N	2025-05-27 15:00:00+00
7cc293fe-82cb-4ad3-81a0-2933ff644cbe	2ff5653d-7a86-499f-9ffd-943b1b1b4a78	93db6be8-c5bb-4bf5-ac84-11fcf9dae304	템포주: 2km, 6:30min/km, 목적: 레이스 페이스 적응과 젖산역치 향상	f	2025-05-28 15:00:00+00
29d93673-58f8-4e97-b1bf-a7e8bd564bd9	2ff5653d-7a86-499f-9ffd-943b1b1b4a78	93db6be8-c5bb-4bf5-ac84-11fcf9dae304	회복주: 2km, 8:30min/km, 목적: 가벼운 유산소로 회복	f	2025-05-29 15:00:00+00
17243111-dcd2-44ae-a939-badd19de6d12	2ff5653d-7a86-499f-9ffd-943b1b1b4a78	93db6be8-c5bb-4bf5-ac84-11fcf9dae304	파틀렉: 2km 예열, 0.1km 스퍼트 X 5회(6:00min/km), 0.2km 조깅 반복, 1km 쿨다운, 목적: 다양한 페이스 적응	f	2025-05-30 15:00:00+00
5e056652-2388-40af-9931-ceecaf87eef9	2ff5653d-7a86-499f-9ffd-943b1b1b4a78	93db6be8-c5bb-4bf5-ac84-11fcf9dae304	장거리 지구력주: 4km, 7:30min/km, 목적: 지구력 증진 및 거리 자신감 확보	f	2025-05-31 15:00:00+00
7e7bc8eb-84dc-4450-8394-a0851c861686	2ff5653d-7a86-499f-9ffd-943b1b1b4a78	93db6be8-c5bb-4bf5-ac84-11fcf9dae304	휴식일: 목적: 신체 회복 및 컨디션 조절	\N	2025-06-01 15:00:00+00
52ad4714-8444-40a4-a87c-d89e235e4be3	2ff5653d-7a86-499f-9ffd-943b1b1b4a78	93db6be8-c5bb-4bf5-ac84-11fcf9dae304	중간 속도 지속주: 2km, 6:40min/km, 목적: 뚜렷한 페이스 감각 유지	f	2025-06-02 15:00:00+00
329ae706-ac01-45a6-9333-24f327e9f341	2ff5653d-7a86-499f-9ffd-943b1b1b4a78	93db6be8-c5bb-4bf5-ac84-11fcf9dae304	회복주: 2km, 8:20min/km, 목적: 근육 피로 해소	f	2025-06-03 15:00:00+00
a81133af-af58-4a45-a8b0-30a7b0f3de23	2ff5653d-7a86-499f-9ffd-943b1b1b4a78	93db6be8-c5bb-4bf5-ac84-11fcf9dae304	템포주: 2.5km, 6:15min/km, 목적: 레이스 전 흐름 점검	f	2025-06-04 15:00:00+00
eacc4536-e2c3-446b-b77c-e41ce2d5c0a4	2ff5653d-7a86-499f-9ffd-943b1b1b4a78	93db6be8-c5bb-4bf5-ac84-11fcf9dae304	회복주: 1.5km, 8:30min/km, 목적: 컨디션 조절 및 피로 완전 제거	f	2025-06-05 15:00:00+00
64f38d9d-6c20-4dca-b3bd-da6abbb114bc	2ff5653d-7a86-499f-9ffd-943b1b1b4a78	93db6be8-c5bb-4bf5-ac84-11fcf9dae304	마라톤: 5km, 6:00–6:30min/km, 목적: 보호된 페이스 유지로 완주 목표 달성	f	2025-06-06 15:00:00+00
f9a6bffd-ce46-4720-af93-184a6ba07bc9	8c02026c-8c05-4302-9de7-b4b84b8a8907	6d084e9d-d896-45d1-af00-1f5063415366	회복주: 4km, 8:00min/km, 목적: 회복 및 가벼운 근육 자극	f	2025-05-21 15:00:00+00
d70404de-142f-4461-9e67-9da090c5c80c	8c02026c-8c05-4302-9de7-b4b84b8a8907	6d084e9d-d896-45d1-af00-1f5063415366	휴식일: 목적: 근육 회복 및 컨디션 조절	\N	2025-05-22 15:00:00+00
53631499-4ffe-4911-b492-8e1834dbf2d6	8c02026c-8c05-4302-9de7-b4b84b8a8907	6d084e9d-d896-45d1-af00-1f5063415366	장거리 지구력주: 7km, 7:30min/km, 목적: 유산소 지구력 확보 및 장거리 적응	f	2025-05-23 15:00:00+00
26e1a3bd-dc6b-41d0-aa81-1be11c1f8f59	8c02026c-8c05-4302-9de7-b4b84b8a8907	6d084e9d-d896-45d1-af00-1f5063415366	휴식일: 목적: 회복 및 피로 관리	\N	2025-05-24 15:00:00+00
8da41879-5e32-4c12-b0c2-1e60deea9a13	8c02026c-8c05-4302-9de7-b4b84b8a8907	6d084e9d-d896-45d1-af00-1f5063415366	중간 속도 지속주: 4km, 6:30min/km(Z3), 목적: 중간 강도 페이스 적응	f	2025-05-25 15:00:00+00
0c748a4a-9485-4fc8-9125-7f0efec2651d	8c02026c-8c05-4302-9de7-b4b84b8a8907	6d084e9d-d896-45d1-af00-1f5063415366	템포주: 2km, 2km, 1km (각각 템포 페이스 5:20min/km, 5:20min/km, 7:30min/km), 목적: 레이스 페이스 감각 및 유산소 역치 향상	f	2025-05-26 15:00:00+00
4546ac37-7b90-4d79-a7bf-a9ece7ab7475	8c02026c-8c05-4302-9de7-b4b84b8a8907	6d084e9d-d896-45d1-af00-1f5063415366	휴식일: 목적: 컨디션 조절 및 근육 회복	\N	2025-05-27 15:00:00+00
6c960c0e-a907-4d1a-929e-8534c7f7192a	8c02026c-8c05-4302-9de7-b4b84b8a8907	6d084e9d-d896-45d1-af00-1f5063415366	인터벌: 0.4km x 3회(4:50min/km), 회복주 0.4km x 3회(8:00min/km), 목적: 대근육 파워 및 속도 자극	f	2025-05-28 15:00:00+00
a0c0755e-dd71-4c08-b897-0a0e500f4e7d	8c02026c-8c05-4302-9de7-b4b84b8a8907	6d084e9d-d896-45d1-af00-1f5063415366	회복주: 3km, 8:00min/km, 목적: 근육 유연성 회복 및 피로 누적 방지	f	2025-05-29 15:00:00+00
5223a7b3-487c-4ed4-9937-11c1b9d5328d	8c02026c-8c05-4302-9de7-b4b84b8a8907	6d084e9d-d896-45d1-af00-1f5063415366	파틀렉: 5km (1km 조깅, 80m 스퍼트 x 4, 조깅 1km, 80m 스퍼트 x 4, 쿨다운 1km), 목적: 변속 적응 및 체력 유지	f	2025-05-30 15:00:00+00
fc9d3f52-8476-4c38-aa8b-268a19b7dba3	8c02026c-8c05-4302-9de7-b4b84b8a8907	6d084e9d-d896-45d1-af00-1f5063415366	10km 마라톤 레이스: 10km, 6:40–7:10min/km(완주 목표), 목적: 최종 목표 달성 및 실제 레이스 경험	f	2025-05-31 15:00:00+00
\.


--
-- TOC entry 3490 (class 0 OID 25994)
-- Dependencies: 216
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: runhwani
--

COPY public.users (user_id, email, password, nickname, profile_image, avg_pace, gender, birthday, fcm_token, height, weight, created_at) FROM stdin;
aac20c90-f1aa-4d3c-9e19-cb9b083031b5	v@v.v	$2a$10$sJ4r39vkojt15WT2VwoVfO5FaAvY4V6DmyChoVdCUk1DcEYFEE8UK	v	https://k12d107.p.ssafy.io/uploads/f5d049d6	\N	MALE	1990-01-01	\N	179	54	2025-05-21 05:20:24.657406+00
f8305fc6-02d1-4271-8742-c08048b2b64f	user33@example.com	$2a$10$./BJ6MXAfu5cby1lO59WBehwbOj8Se2EV.aGIghhk5MDaJneJJqLO	러너33	\N	\N	MALE	1990-01-01	eEw23SDFsdf...	175.5	68.5	2025-05-21 05:20:57.022247+00
0a6455c5-c096-4a30-8e26-26f7e64aefb2	test1@example.com	$2a$10$7uy9sx4et46DTjn7v68N1ObURByx5nD7mFYlIbqujMOStaA.6rtCq	유저1	https://k12d107.p.ssafy.io/uploads/bc9fd080.jpg	360	MALE	1999-01-01	eEw23SDFsdf...	173	68.5	2025-05-21 04:25:43.830473+00
fb236feb-bcd0-4d21-ae70-5450364291bc	test2@example.com	$2a$10$4jeGA3ZQemlel51gaJEe7OcLDjgVnnvn.xhcZkDlniSNEX/se8vrK	유저2	https://k12d107.p.ssafy.io/uploads/0b758ac8.jpg	330	MALE	1990-01-01	eEw23SDFsdf...	165	53	2025-05-21 04:33:20.528505+00
9281d52c-1b27-442e-872a-bb001d5b8d51	test3@example.com	$2a$10$V8iUqjPG8CtAdGL9uL06xu5dkmk9Hpfgl2kUNKDz1t0v97NCnHkIG	유저3	https://k12d107.p.ssafy.io/uploads/cd10b08b.jpg	296	MALE	1990-01-01	eEw23SDFsdf...	185	87	2025-05-21 04:34:30.522391+00
1206a60d-2489-42c3-b67d-5dfc3948acfe	m@m.com	$2a$10$hL0bJn38L62DRXNSpFPMyeSg2cBI14OWpPLvUbMIY5mReHJdqLA1q	mm	https://k12d107.p.ssafy.io/uploads/0afe8160.jpg	590.0845184326172	MALE	1999-01-01	dUu4vWZqRBO_t2RNd_-0yd:APA91bHBrS4c6BnF0zSq7ohCQSMN35qEJnLPBsv5alXiF67FtrCbsryPf5Vde38Bz53_FPOBwwxjTeRdj0mR4D6EOj8Xyn8tzF2GbLWyBdYdj2_hQMQvYfE	160	65	2025-05-21 13:23:27.139786+00
15b88134-e3c4-4822-9bfa-244c6a185913	b@b.b	$2a$10$nFxNjuG.DEBmZBhRS2oNv.etuHhRc/s15TYUvF3DtIPPkJTAkuf0m	러너1	\N	\N	MALE	1990-01-01	doQhcOP1T2qn0_4ovknSPM:APA91bHpzHNWICBUklQJQ6QEvZrPEruxJ-jGscy37yREe2zHqyyyTxbq_A3VAdcJHl1U9DS5Jcx584lwNHABZ9NeiVb1ZO0wK7IVqqx4KYkB14l4j3LXenc	175.5	68.5	2025-05-21 17:09:32.289667+00
dfeff5ca-d525-4062-892a-f56b3844f724	t@t.t	$2a$10$ZlRjz8HQ2MXV.33B0w2Cf.KN54bKvcYD.5ozcX5ET8UX2VcZUcQUS	러너1	\N	\N	MALE	1990-01-01	eEw23SDFsdf...	175.5	68.5	2025-05-21 16:00:05.798892+00
93db6be8-c5bb-4bf5-ac84-11fcf9dae304	run@mate.com	$2a$10$..Iv8K7/NPmLbR2xpgRacuYzr.sEd.IB/Naid5OWGe15RQiWlWYOW	RunMate	https://k12d107.p.ssafy.io/uploads/81444a20.png	\N	MALE	1990-01-01	daZvpXYGTuy1FUlA9W0Enb:APA91bEIVprANsbfJcQXd3Nfc_vI4oLwsHNJlYxy9vxudKodlH11QTljJZ2O_4nSQMpJxFX7TNhFWjvLqybyqCMT4XnyRqMJyMzM2tGVr9jjM_8eTWURWTA	175.5	68.5	2025-05-22 00:11:50.343101+00
962c3730-2d81-43fb-a4ac-30e2196a81c3	a2@aa.aa	$2a$10$jRIGo2pQLBMoi9hKEzv09ewBwmJVOapIHUv66ATq5Miz3HIAKDh.a	달려라화니	\N	2344.1590576171875	MALE	2000-01-01	de3KhxMRSwyYvA-hLwl_eS:APA91bFwuHbF0DB2uwXlAneGMW-ys2UP8aEGgRzWzXD2k2oMEHLUFoNV5M20j9_UusWGKo9H2J5rkwFGWc0f5Y-KJA0AsQMHfBkMA3hzpuEojPZshf_2f1Y	165	72	2025-05-21 07:35:27.17912+00
e68628ca-fc59-49e5-afb6-b38091def7e2	user3@example.com	$2a$10$ud.muhMx49wPgo0tFM1cpe.6YbIe31ZXqjlmE2JBqD8gkJORKgvnO	러너1	https://k12d107.p.ssafy.io/uploads/d4eaf03b.png	\N	MALE	1990-01-01	eEw23SDFsdf...	175.5	68.5	2025-05-21 16:10:41.967813+00
6ffb74df-9bc4-41ed-b2b8-2b949e07f0b2	k@k.com	$2a$10$/KWpfZbE7cdICvVCGN4jie7dt2KhEKhU44rYz2nKTZIGPT5F2zeme	kk	https://k12d107.p.ssafy.io/uploads/2a1c3790.jpg	1728.100986687112	MALE	2000-01-01	ehOZ1VdKSVinpAZ_uGdja0:APA91bEZKYPZeDWbIvnngutVSonzohNd0KDpR3RkYPHKDlfvV5SoihLdUJ5T_RxATi0Paw60_mfydGydkb7V4wAd10x91OpyIuGi0YHwAt6i03oHFnYuTa4	166	66	2025-05-21 15:47:07.934125+00
bb87e298-e9a8-43e1-a528-75082d365036	u1@e.com	$2a$10$qzoZVxkABGZTINGjsTvDxu/DqLEgTzngPtZcppklLAmLTySoumKva	러너1	\N	482.24676513671875	MALE	1990-01-01	erGCATj4QYKGeIPkxvNM6z:APA91bHfMbS03P0vu9PYdkSeyYn8XVKmLIPB1t64dBPKL30G_oDlg8B18f7wByajhzacyr4HpjzthhKj1wg4_W_cmNtSukSRRQnP0HdRsOyFe_2tusFKiyQ	175.5	68.5	2025-05-21 12:47:54.312435+00
1a27f815-0c6c-4bf1-b2c9-9cd88dd9ea44	z@z.z	$2a$10$GXPMfw0C55vkgqFUhojiS.EVOuFWPskXAgKmhBahEISRMuTt.Tkau	z	https://k12d107.p.ssafy.io/uploads/6ce0c0ff.png	\N	MALE	1990-01-01	\N	179	54	2025-05-21 05:09:02.883112+00
a886fafd-b8c3-4c81-91af-dc3df66c71bd	u@e.com	$2a$10$YMRtyTg1nl0xGK.TuPYBWOuuYFkteGb9GS4/8XoMAEak/pzpx0EZS	러너1	\N	979.9050713591855	MALE	1990-01-01	cgjg49f0S2eU2K3IsTp9iC:APA91bFM0YqfZDwwjerrZbuIMF-YqkBfRmCZEar-jyZQqgdntpZVP4ugQCfuyBo9IiqKt0uKrAGuieeH-TGF_sXWaQX6yP4ski7hQX8QjeRwXwKoX8H2PYI	175.5	68.5	2025-05-21 04:18:57.083147+00
978a211a-d660-404b-b89e-bd115fc48532	x@x.x	$2a$10$dpoDqpr0pOw0Lgirel6fnuSTVwYiEpVFQRCtAryQ3/XES5UcIHhRC	x	https://k12d107.p.ssafy.io/uploads/1b4ca2e0	42.927525207439324	MALE	1990-01-01	eHCAcCRcQSu12_HpgblHtF:APA91bFbHSSrYi0EgT7ogX9VrD2TwvE5s9JoEHW6xsw7m9k17kNVMglBZ7fo3Mx9D0r5VuP4ijNGEoWOq9xYRTCS-bkWJmMdQTYPa6Uwp2k0WbWD9FqYzK4	179	54	2025-05-21 05:10:29.753777+00
063cf97b-0c40-4c82-a4f7-4f711ea11578	jhanoo@naver.com	$2a$10$txfdv4xyGHltqQD1Uc7DNOGJNxvUG7eFki40PDIf.Fn/fcS3XFkTG	쟈누	https://k12d107.p.ssafy.io/uploads/3a726232.jpg	1533.6018166285414	MALE	1998-04-04	eaQ6RF3oTZ6ExvZTZywBpD:APA91bE2DX8-rRV4y1BoBvk6gTZdFocHoPHQKF-xv4d7_RcGTtZXI8p4BUcI_lrp1cjzwEw1qkUBYoMNn6tjY-4fi4AeyRhu3xfbyzmD0dB5j8KiDD_t-9A	170	70	2025-05-21 04:41:28.713157+00
0f9c3869-310c-48c5-b838-c50fa0ad5bc1	p@p.p	$2a$10$GmOnCyoUsz2jChNlHm4NUe/xdcZ5Y/QsAADbW2E66xpQFKQOS4vHG	러너1	\N	56.565273429891555	MALE	1990-01-01	eEw23SDFsdf...	175.5	68.5	2025-05-21 16:12:24.222254+00
417b3d0e-c29e-4840-a043-415f15c45b6b	s@s.s	$2a$10$XUjL9XYKIm9b4WzOfyOJIOEYMLOOL6cekZGfoXb/BmeU8kvEZlh6S	v	\N	53.1105296295844	MALE	1990-01-01	\N	179	54	2025-05-21 05:20:50.567838+00
6d084e9d-d896-45d1-af00-1f5063415366	runmate@ssafy.com	$2a$10$DnNGO.Evamy1nM0Ag30tJulBCE16Q2t6l6aA9Cz3jNuh4Asvo8exm	러너1	https://k12d107.p.ssafy.io/uploads/77eb7d9b.png	6246.200347900391	MALE	1990-01-01	eEw23SDFsdf...	175.5	68.5	2025-05-22 01:48:45.129913+00
a243b676-df47-42c3-a6cb-b91d60410ffd	a@a.a	$2a$10$eUCfnlBIwT/yIKDdmmGA0O.p2Hv4Sg3QXqYF9pA0/jx8VkPzrREb.	a	https://k12d107.p.ssafy.io/uploads/7fd36dd2.png	170.60477047733286	MALE	1990-01-01	daZvpXYGTuy1FUlA9W0Enb:APA91bEIVprANsbfJcQXd3Nfc_vI4oLwsHNJlYxy9vxudKodlH11QTljJZ2O_4nSQMpJxFX7TNhFWjvLqybyqCMT4XnyRqMJyMzM2tGVr9jjM_8eTWURWTA	179	54	2025-05-21 04:15:44.731998+00
\.


--
-- TOC entry 3305 (class 2606 OID 26024)
-- Name: course_likes course_likes_pkey; Type: CONSTRAINT; Schema: public; Owner: runhwani
--

ALTER TABLE ONLY public.course_likes
    ADD CONSTRAINT course_likes_pkey PRIMARY KEY (like_id);


--
-- TOC entry 3302 (class 2606 OID 26012)
-- Name: courses courses_pkey; Type: CONSTRAINT; Schema: public; Owner: runhwani
--

ALTER TABLE ONLY public.courses
    ADD CONSTRAINT courses_pkey PRIMARY KEY (course_id);


--
-- TOC entry 3309 (class 2606 OID 26041)
-- Name: curricula curricula_pkey; Type: CONSTRAINT; Schema: public; Owner: runhwani
--

ALTER TABLE ONLY public.curricula
    ADD CONSTRAINT curricula_pkey PRIMARY KEY (curriculum_id);


--
-- TOC entry 3317 (class 2606 OID 26077)
-- Name: group_members group_members_pkey; Type: CONSTRAINT; Schema: public; Owner: runhwani
--

ALTER TABLE ONLY public.group_members
    ADD CONSTRAINT group_members_pkey PRIMARY KEY (member_id);


--
-- TOC entry 3313 (class 2606 OID 26059)
-- Name: groups groups_pkey; Type: CONSTRAINT; Schema: public; Owner: runhwani
--

ALTER TABLE ONLY public.groups
    ADD CONSTRAINT groups_pkey PRIMARY KEY (group_id);


--
-- TOC entry 3321 (class 2606 OID 26094)
-- Name: histories histories_pkey; Type: CONSTRAINT; Schema: public; Owner: runhwani
--

ALTER TABLE ONLY public.histories
    ADD CONSTRAINT histories_pkey PRIMARY KEY (history_id);


--
-- TOC entry 3327 (class 2606 OID 26115)
-- Name: marathon_distances marathon_distances_pkey; Type: CONSTRAINT; Schema: public; Owner: runhwani
--

ALTER TABLE ONLY public.marathon_distances
    ADD CONSTRAINT marathon_distances_pkey PRIMARY KEY (distance_id);


--
-- TOC entry 3296 (class 2606 OID 25993)
-- Name: marathons marathons_pkey; Type: CONSTRAINT; Schema: public; Owner: runhwani
--

ALTER TABLE ONLY public.marathons
    ADD CONSTRAINT marathons_pkey PRIMARY KEY (marathon_id);


--
-- TOC entry 3331 (class 2606 OID 26128)
-- Name: todos todos_pkey; Type: CONSTRAINT; Schema: public; Owner: runhwani
--

ALTER TABLE ONLY public.todos
    ADD CONSTRAINT todos_pkey PRIMARY KEY (todo_id);


--
-- TOC entry 3298 (class 2606 OID 26004)
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: runhwani
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- TOC entry 3300 (class 2606 OID 26002)
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: runhwani
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);


--
-- TOC entry 3306 (class 1259 OID 26141)
-- Name: idx_course_likes_course_id; Type: INDEX; Schema: public; Owner: runhwani
--

CREATE INDEX idx_course_likes_course_id ON public.course_likes USING btree (course_id);


--
-- TOC entry 3307 (class 1259 OID 26140)
-- Name: idx_course_likes_user_id; Type: INDEX; Schema: public; Owner: runhwani
--

CREATE INDEX idx_course_likes_user_id ON public.course_likes USING btree (user_id);


--
-- TOC entry 3303 (class 1259 OID 26139)
-- Name: idx_courses_created_by; Type: INDEX; Schema: public; Owner: runhwani
--

CREATE INDEX idx_courses_created_by ON public.courses USING btree (created_by);


--
-- TOC entry 3310 (class 1259 OID 26143)
-- Name: idx_curricula_marathon_id; Type: INDEX; Schema: public; Owner: runhwani
--

CREATE INDEX idx_curricula_marathon_id ON public.curricula USING btree (marathon_id);


--
-- TOC entry 3311 (class 1259 OID 26142)
-- Name: idx_curricula_user_id; Type: INDEX; Schema: public; Owner: runhwani
--

CREATE INDEX idx_curricula_user_id ON public.curricula USING btree (user_id);


--
-- TOC entry 3318 (class 1259 OID 26146)
-- Name: idx_group_members_group_id; Type: INDEX; Schema: public; Owner: runhwani
--

CREATE INDEX idx_group_members_group_id ON public.group_members USING btree (group_id);


--
-- TOC entry 3319 (class 1259 OID 26147)
-- Name: idx_group_members_user_id; Type: INDEX; Schema: public; Owner: runhwani
--

CREATE INDEX idx_group_members_user_id ON public.group_members USING btree (user_id);


--
-- TOC entry 3314 (class 1259 OID 26145)
-- Name: idx_groups_course_id; Type: INDEX; Schema: public; Owner: runhwani
--

CREATE INDEX idx_groups_course_id ON public.groups USING btree (course_id);


--
-- TOC entry 3315 (class 1259 OID 26144)
-- Name: idx_groups_leader_id; Type: INDEX; Schema: public; Owner: runhwani
--

CREATE INDEX idx_groups_leader_id ON public.groups USING btree (leader_id);


--
-- TOC entry 3322 (class 1259 OID 26149)
-- Name: idx_histories_course_id; Type: INDEX; Schema: public; Owner: runhwani
--

CREATE INDEX idx_histories_course_id ON public.histories USING btree (course_id);


--
-- TOC entry 3323 (class 1259 OID 26150)
-- Name: idx_histories_group_id; Type: INDEX; Schema: public; Owner: runhwani
--

CREATE INDEX idx_histories_group_id ON public.histories USING btree (group_id);


--
-- TOC entry 3324 (class 1259 OID 26148)
-- Name: idx_histories_user_id; Type: INDEX; Schema: public; Owner: runhwani
--

CREATE INDEX idx_histories_user_id ON public.histories USING btree (user_id);


--
-- TOC entry 3325 (class 1259 OID 26151)
-- Name: idx_marathon_distances_marathon_id; Type: INDEX; Schema: public; Owner: runhwani
--

CREATE INDEX idx_marathon_distances_marathon_id ON public.marathon_distances USING btree (marathon_id);


--
-- TOC entry 3328 (class 1259 OID 26152)
-- Name: idx_todos_curriculum_id; Type: INDEX; Schema: public; Owner: runhwani
--

CREATE INDEX idx_todos_curriculum_id ON public.todos USING btree (curriculum_id);


--
-- TOC entry 3329 (class 1259 OID 26153)
-- Name: idx_todos_user_id; Type: INDEX; Schema: public; Owner: runhwani
--

CREATE INDEX idx_todos_user_id ON public.todos USING btree (user_id);


--
-- TOC entry 3333 (class 2606 OID 26030)
-- Name: course_likes course_likes_course_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: runhwani
--

ALTER TABLE ONLY public.course_likes
    ADD CONSTRAINT course_likes_course_id_fkey FOREIGN KEY (course_id) REFERENCES public.courses(course_id);


--
-- TOC entry 3334 (class 2606 OID 26025)
-- Name: course_likes course_likes_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: runhwani
--

ALTER TABLE ONLY public.course_likes
    ADD CONSTRAINT course_likes_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- TOC entry 3332 (class 2606 OID 26013)
-- Name: courses courses_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: runhwani
--

ALTER TABLE ONLY public.courses
    ADD CONSTRAINT courses_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(user_id);


--
-- TOC entry 3335 (class 2606 OID 26047)
-- Name: curricula curricula_marathon_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: runhwani
--

ALTER TABLE ONLY public.curricula
    ADD CONSTRAINT curricula_marathon_id_fkey FOREIGN KEY (marathon_id) REFERENCES public.marathons(marathon_id);


--
-- TOC entry 3336 (class 2606 OID 26042)
-- Name: curricula curricula_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: runhwani
--

ALTER TABLE ONLY public.curricula
    ADD CONSTRAINT curricula_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- TOC entry 3339 (class 2606 OID 26078)
-- Name: group_members group_members_group_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: runhwani
--

ALTER TABLE ONLY public.group_members
    ADD CONSTRAINT group_members_group_id_fkey FOREIGN KEY (group_id) REFERENCES public.groups(group_id);


--
-- TOC entry 3340 (class 2606 OID 26083)
-- Name: group_members group_members_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: runhwani
--

ALTER TABLE ONLY public.group_members
    ADD CONSTRAINT group_members_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- TOC entry 3337 (class 2606 OID 26065)
-- Name: groups groups_course_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: runhwani
--

ALTER TABLE ONLY public.groups
    ADD CONSTRAINT groups_course_id_fkey FOREIGN KEY (course_id) REFERENCES public.courses(course_id);


--
-- TOC entry 3338 (class 2606 OID 26060)
-- Name: groups groups_leader_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: runhwani
--

ALTER TABLE ONLY public.groups
    ADD CONSTRAINT groups_leader_id_fkey FOREIGN KEY (leader_id) REFERENCES public.users(user_id);


--
-- TOC entry 3341 (class 2606 OID 26100)
-- Name: histories histories_course_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: runhwani
--

ALTER TABLE ONLY public.histories
    ADD CONSTRAINT histories_course_id_fkey FOREIGN KEY (course_id) REFERENCES public.courses(course_id);


--
-- TOC entry 3342 (class 2606 OID 26105)
-- Name: histories histories_group_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: runhwani
--

ALTER TABLE ONLY public.histories
    ADD CONSTRAINT histories_group_id_fkey FOREIGN KEY (group_id) REFERENCES public.groups(group_id);


--
-- TOC entry 3343 (class 2606 OID 26095)
-- Name: histories histories_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: runhwani
--

ALTER TABLE ONLY public.histories
    ADD CONSTRAINT histories_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- TOC entry 3344 (class 2606 OID 26116)
-- Name: marathon_distances marathon_distances_marathon_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: runhwani
--

ALTER TABLE ONLY public.marathon_distances
    ADD CONSTRAINT marathon_distances_marathon_id_fkey FOREIGN KEY (marathon_id) REFERENCES public.marathons(marathon_id);


--
-- TOC entry 3345 (class 2606 OID 26129)
-- Name: todos todos_curriculum_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: runhwani
--

ALTER TABLE ONLY public.todos
    ADD CONSTRAINT todos_curriculum_id_fkey FOREIGN KEY (curriculum_id) REFERENCES public.curricula(curriculum_id);


--
-- TOC entry 3346 (class 2606 OID 26134)
-- Name: todos todos_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: runhwani
--

ALTER TABLE ONLY public.todos
    ADD CONSTRAINT todos_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id);


-- Completed on 2025-05-22 11:11:52

--
-- PostgreSQL database dump complete
--

