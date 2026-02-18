FROM node:20-alphine AS build
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm ci || npm install
COPY . .
RUN npm run build

FROM nginx:alphine
COPY --from=build /app/dist /user/share/nginx/html
EXPOSE 90