# aws-lambda-tour-de-france
 AWS Lambda Tour de France is a serverless application built to provide comprehensive and real-time information about the Tour de France, an iconic cycling race. 


![Cloud Architecture](https://github.com/CristhianCuero/aws-lambda-tour-de-france/assets/12468359/64a941fa-3d3e-4f24-877a-c82aef2119c9)


 
## Getting Started

To deploy and use the AWS Lambda Tour de France service, follow these steps:

1. Clone this repository to your local machine.

2. Install the Serverless Framework:
   ```shell
   npm install -g serverless
3. Deploy the service to AWS:
   ```shell
   serverless deploy
   
# Project Structure
The "Tour de France API" follows a basic AWS Lambda Serverless application structure:

- `com.serverless package`: This package serves as the core for all the source code of the project.

- `serverless.yml`: This YAML file is used to configure the serverless application infrastructure. It defines API paths, resources, environment variables, permissions, and more. The serverless CLI utilizes this file to set up the CloudFormation instructions for the application.

- `Handler`: Handlers can be created and linked in the serverless.yml file with API endpoints (optional). These handlers act as the entry points for the serverless API, allowing you to define the behavior for various endpoints.

# Tour de France API Endpoints

The service provides the following API endpoints:

| Name                  | Path                  | Method | Description                                                   |
|-----------------------|-----------------------|--------|---------------------------------------------------------------|
| Health Endpoint       | `/health`             | GET    | Check the operational status and health of the API.           |
| General Results Endpoint | `/rankings`         | GET    | View the general result table of the Tour de France, including overall standings of cyclists. |
| Stage Results Endpoint | `/rankings/{stage}`   | GET    | Retrieve detailed results for a specific stage of the Tour de France. |


## Authentication and Authorization

The service uses Amazon Cognito for user authentication and authorization. Access to endpoints is controlled by OAuth2 scopes, allowing fine-grained access control.

## Environment Variables

The service uses the following environment variables:

- `REGION`: AWS region for deployment.
- `FINISHERS_TABLE`: DynamoDB table for storing finisher data.
- `STAGES_TABLE`: DynamoDB table for storing stage data.
- `API_AUTHORIZER`: Cognito authorizer for API endpoints.
- `COGNITO_USER_POOL`: Cognito user pool name.
- `COGNITO_DOMAIN`: Cognito user pool domain.
- `COGNITO_CLIENT`: Cognito user pool client name.

## IAM Permissions

The service's IAM role has permissions for DynamoDB operations and other necessary actions.

## Cognito Setup

The service sets up Cognito user pool, user pool resource server, user pool client, and user pool domain for authentication and authorization.
Feel free to customize the content further to match your specific project details and formatting preferences.





