# Task Management Protocol Documentation

## Connection Protocol
### HELLO
- Purpose: Establish connection with server
- Request: `HELLO`
- Response: 
  - Success: `PROJL [json_project_list]` - Returns list of all projects
  - Failure: `ERROR [error_message]`

## Project Operations
### PROJS (Project Select)
- Purpose: Select/request a specific project
- Request: `PROJS [project_name]`
- Parameters:
  - project_name: String identifier of the project

### PROJD (Project Data)
- Purpose: Server response containing project details
- Response: 
  - Success: `PROJD [project_data]`
    ```json
    {
      "name": "string",
      "tasks": ["task1", "task2", ...],
      "creation_date": "date",
      // other project metadata
    }
    ```
  - Failure: `ERROR [error_message]`

## Project Modifications
### ADDPR (Add Project)
- Purpose: Create a new project
- Request: `ADDPR [project_info]`
- Parameters:
  ```json
  {
    "name": "string",
    "description": "string",
    // other project metadata
  }
- Response:
    - Success: `OKAYY`
    - Failure: `ERROR [error_message]`

### DELPR (Delete Project)
- Purpose: Remove an existing project
- Request: `DELPR [project_name]`
- Parameters:
    - project_name: String identifier of project to delete
- Response:
    - Success: `OKAYY`
    - Failure: `ERROR [error_message]`

## Task Operations
### ADDTS (Add Task)
- Purpose: Create a new task in current project
- Request: `ADDTS [task_info]`
- Parameters:
    ```json
    {
      "name": "string",
      "description": "string",
      "deadline": "date",
      // other task metadata
    }
    ```
- Response:
    - Success: `OKAYY`
    - Failure: `ERROR [error_message]`

### DELTS (Delete Task)
- Purpose: Remove an existing task
- Request: `DELTS [task_name]`
- Parameters:
  - task_name: String identifier of task to delete
- Response:
  - Success: `OKAYY`
  - Failure: `ERROR [error_message]`

### GETTS (Get Task)
- Purpose: Request details of a specific task
- Request: `GETTS [task_name]`
- Parameters:
  - task_name: String identifier of task
- Response:
  - Success: `TASKD [task_data]`
  ```json
  {
    "name": "string",
    "description": "string",
    "status": "string",
    "deadline": "date",
    // other task metadata
  }
  ```
  - Failure: `ERROR [error_message]`

### MODTS (Modify Task)
- Purpose: Update an existing task
- Request: `MODTS [task_name] [task_data]`
- Parameters:
  - task_name: String identifier of task
  - task_data: Updated task information
  ```json
  {
    "description": "string",
    "status": "string",
    "deadline": "date",
    // modifiable task fields
  }
  ```
- Response:
  - Success: `OKAYY`
  - Failure: `ERROR [error_message]`

## Response Codes
### OKAYY
- Indicates successful completion of requested operation
### ERROR
- Indicates operation failure
- Always accompanied by error message explaining the failure reason

