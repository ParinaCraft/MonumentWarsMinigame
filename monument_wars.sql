/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : PostgreSQL
 Source Server Version : 90603
 Source Host           : localhost:5432
 Source Catalog        : parina
 Source Schema         : monument_wars

 Target Server Type    : PostgreSQL
 Target Server Version : 90603
 File Encoding         : 65001

 Date: 31/01/2014 00:00:41
*/


-- ----------------------------
-- Sequence structure for users_stats_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "monument_wars"."users_stats_id_seq";
CREATE SEQUENCE "monument_wars"."users_stats_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Table structure for users_stats
-- ----------------------------
DROP TABLE IF EXISTS "monument_wars"."users_stats";
CREATE TABLE "monument_wars"."users_stats" (
  "id" int4 NOT NULL DEFAULT nextval('"monument_wars".users_stats_id_seq'::regclass),
  "kills" int4 NOT NULL DEFAULT 0,
  "deaths" int4 NOT NULL DEFAULT 0,
  "plays" int4 NOT NULL DEFAULT 0,
  "wins" int4 NOT NULL DEFAULT 0
)
;

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "monument_wars"."users_stats_id_seq"
OWNED BY "monument_wars"."users_stats"."id";
SELECT setval('"monument_wars"."users_stats_id_seq"', 2, false);
