/* Targets */
CREATE TABLE st_event_calendars (
  id                     VARCHAR(50) NOT NULL,
  instanceId             VARCHAR(50),
  targetResourceUniqueId VARCHAR(128),
  createDate             TIMESTAMP   NOT NULL,
  createdBy              VARCHAR(50) NOT NULL,
  lastUpdateDate         TIMESTAMP,
  lastUpdatedBy          VARCHAR(50),
  version                INT8        NOT NULL
);

ALTER TABLE st_event_calendars ADD CONSTRAINT st_event_calendars_pk PRIMARY KEY (id);

/* Indexes */
CREATE UNIQUE INDEX idx_uc_st_event_calendars ON st_event_calendars (instanceId, targetResourceUniqueId);

/* Events */
CREATE TABLE st_events (
  id             VARCHAR(50)  NOT NULL,
  calendarId     VARCHAR(50)  NOT NULL,
  eventType      VARCHAR(50)  NOT NULL,
  privacyType    VARCHAR(50)  NOT NULL,
  eventPriority  INT4         NOT NULL,
  title          VARCHAR(255) NOT NULL,
  duration       INT8         NOT NULL,
  createDate     TIMESTAMP    NOT NULL,
  createdBy      VARCHAR(50)  NOT NULL,
  lastUpdateDate TIMESTAMP,
  lastUpdatedBy  VARCHAR(50),
  version        INT8         NOT NULL
);

ALTER TABLE st_events ADD CONSTRAINT st_events_pk PRIMARY KEY (id);

/* Infos */
CREATE TABLE st_event_infos (
  id             VARCHAR(50) NOT NULL,
  eventId        VARCHAR(50) NOT NULL,
  infoType       VARCHAR(50) NOT NULL,
  content        VARCHAR     NOT NULL,
  createDate     TIMESTAMP   NOT NULL,
  createdBy      VARCHAR(50) NOT NULL,
  lastUpdateDate TIMESTAMP,
  lastUpdatedBy  VARCHAR(50),
  version        INT8        NOT NULL
);

ALTER TABLE st_event_infos ADD CONSTRAINT st_event_infos_pk PRIMARY KEY (id);

/* Participant */
CREATE TABLE st_event_attendees (
  id                  VARCHAR(50) NOT NULL,
  eventId             VARCHAR(50) NOT NULL,
  attendeeType        VARCHAR(50) NOT NULL,
  attendeeId          VARCHAR(50) NOT NULL,
  participationStatus VARCHAR(50) NOT NULL,
  createDate          TIMESTAMP   NOT NULL,
  createdBy           VARCHAR(50) NOT NULL,
  lastUpdateDate      TIMESTAMP,
  lastUpdatedBy       VARCHAR(50),
  version             INT8        NOT NULL
);

ALTER TABLE st_event_attendees ADD CONSTRAINT st_event_attendees_pk PRIMARY KEY (id);

/* Planning */
CREATE TABLE st_event_planning (
  id             VARCHAR(50) NOT NULL,
  eventId        VARCHAR(50) NOT NULL,
  beginDate      INT8        NOT NULL,
  endDate        INT8        NOT NULL,
  createDate     TIMESTAMP   NOT NULL,
  createdBy      VARCHAR(50) NOT NULL,
  lastUpdateDate TIMESTAMP,
  lastUpdatedBy  VARCHAR(50),
  version        INT8        NOT NULL
);

ALTER TABLE st_event_planning ADD CONSTRAINT st_event_planning_pk PRIMARY KEY (id);

/* Recurrences */
CREATE TABLE st_event_recurrences (
  id                     VARCHAR(50) NOT NULL,
  planningId             VARCHAR(50) NOT NULL,
  recurrenceType         VARCHAR(50) NOT NULL,
  every                  INT4        NOT NULL,
  daysOfWeek             CHAR(7)     NOT NULL,
  recurrenceMonthType    VARCHAR(50) NOT NULL,
  endingAt               INT8        NOT NULL,
  endingAfterXoccurences INT4        NOT NULL,
  createDate             TIMESTAMP   NOT NULL,
  createdBy              VARCHAR(50) NOT NULL,
  lastUpdateDate         TIMESTAMP,
  lastUpdatedBy          VARCHAR(50),
  version                INT8        NOT NULL
);

ALTER TABLE st_event_recurrences ADD CONSTRAINT st_event_recurrences_pk PRIMARY KEY (id);

/* Exceptions */
CREATE TABLE st_event_exceptions (
  id             VARCHAR(50) NOT NULL,
  recurrenceId   VARCHAR(50) NOT NULL,
  beginDate      INT8        NOT NULL,
  endDate        INT8        NOT NULL,
  createDate     TIMESTAMP   NOT NULL,
  createdBy      VARCHAR(50) NOT NULL,
  lastUpdateDate TIMESTAMP,
  lastUpdatedBy  VARCHAR(50),
  version        INT8        NOT NULL
);

ALTER TABLE st_event_exceptions ADD CONSTRAINT st_event_exceptions_pk PRIMARY KEY (id);

/* Reminds */
CREATE TABLE st_event_reminds (
  id              VARCHAR(50) NOT NULL,
  eventId         VARCHAR(50) NOT NULL,
  timeBeforeStart INT8        NOT NULL,
  createDate      TIMESTAMP   NOT NULL,
  createdBy       VARCHAR(50) NOT NULL,
  lastUpdateDate  TIMESTAMP,
  lastUpdatedBy   VARCHAR(50),
  version         INT8        NOT NULL
);

ALTER TABLE st_event_reminds ADD CONSTRAINT st_event_reminds_pk PRIMARY KEY (id);

/* User settings */
CREATE TABLE st_event_user_settings (
  id             VARCHAR(50) NOT NULL,
  calendarId     VARCHAR(50) NOT NULL,
  eventId        VARCHAR(50) NOT NULL,
  rgbColor       VARCHAR(16),
  createDate     TIMESTAMP   NOT NULL,
  createdBy      VARCHAR(50) NOT NULL,
  lastUpdateDate TIMESTAMP,
  lastUpdatedBy  VARCHAR(50),
  version        INT8        NOT NULL
);

ALTER TABLE st_event_user_settings ADD CONSTRAINT st_event_user_settings_pk PRIMARY KEY (id);