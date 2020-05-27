/* version 0.65 */

CREATE TABLE {0}_form_field (
  field varchar(50) NOT NULL default '',
  name varchar(255) NOT NULL default '',
  description varchar(255) NOT NULL default '',
  type varchar(20) NOT NULL default '',
  size varchar(20) NOT NULL default '',
  required INT(10) NOT NULL default 1,
  display_order int(11) NOT NULL default 0,
  update_date datetime NULL,
  update_user_id varchar(100) NOT NULL,
  PRIMARY KEY  (field)
);

CREATE TABLE {0}_form_field_option (
  field varchar(50) NOT NULL default '',
  value varchar(50) NOT NULL default '',
  description varchar(255) NOT NULL default '',
  display_order int(11) NOT NULL default 0,
  update_date datetime NULL,
  update_user_id varchar(100) NOT NULL,
  PRIMARY KEY  (field,value)
);

CREATE TABLE {0}_instance (
  instance_id INTEGER NOT NULL AUTO_INCREMENT,
  user_agent varchar(255) NOT NULL default '',
  ip varchar(30) NOT NULL default '',
  start_time datetime NULL,
  end_time datetime NULL,
  trial_set_number INTEGER NULL,
  test_result INTEGER NULL,
  mean_snr DOUBLE NULL,
  trials_csv TEXT,
  PRIMARY KEY  (instance_id)
);

CREATE TABLE {0}_instance_field (
  instance_id INTEGER NOT NULL,
  field varchar(50) NOT NULL default '',
  value varchar(50) NOT NULL default '',
  PRIMARY KEY  (instance_id, field)
);

/* version 0.73 */

ALTER TABLE {0}_instance ADD COLUMN mode varchar(1) NOT NULL default '';

/* version 0.74 */

ALTER TABLE {0}_instance ADD COLUMN other_instance_id INTEGER NULL;

/* version 0.76 */
ALTER TABLE {0}_instance ADD COLUMN language VARCHAR(10) NULL;

/* version 0.80 */ 
ALTER TABLE {0}_form_field MODIFY COLUMN field varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL default '';
ALTER TABLE {0}_form_field MODIFY COLUMN name varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL default '';
ALTER TABLE {0}_form_field MODIFY COLUMN description varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL default '';
ALTER TABLE {0}_form_field MODIFY COLUMN update_user_id varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL;
ALTER TABLE {0}_form_field_option MODIFY COLUMN field varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL default '';
ALTER TABLE {0}_form_field_option MODIFY COLUMN value varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL default '';
ALTER TABLE {0}_form_field_option MODIFY COLUMN description varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL default '';
ALTER TABLE {0}_form_field_option MODIFY COLUMN update_user_id varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL;
ALTER TABLE {0}_instance_field MODIFY COLUMN field varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL default '';
ALTER TABLE {0}_instance_field MODIFY COLUMN value varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL default '';


/* version 0.92 */

CREATE TABLE {0}_trial (
  instance_id INTEGER NOT NULL AUTO_INCREMENT,
  trial_number INTEGER NOT NULL,
  correct_answer VARCHAR(10) NOT NULL,
  decibels_signal INTEGER NULL,
  participant_answer VARCHAR(10) NULL,
  PRIMARY KEY  (instance_id, trial_number)
);
