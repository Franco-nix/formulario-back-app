import { ValidationPipe } from '@nestjs/common';
import { NestFactory } from '@nestjs/core';
import { NestExpressApplication } from '@nestjs/platform-express';
import { AppModule } from './app.module.js';
import { UPLOADS_DIR } from './personas/personas.controller.js';

async function bootstrap() {
  const app = await NestFactory.create<NestExpressApplication>(AppModule);
  app.enableCors();
  app.useGlobalPipes(new ValidationPipe({ whitelist: true }));
  // Las fotos subidas se sirven en http://<host>:<puerto>/uploads/<archivo>
  app.useStaticAssets(UPLOADS_DIR, { prefix: '/uploads/' });
  // 0.0.0.0 para que el emulador / celular en la LAN puedan conectarse
  await app.listen(process.env.PORT ?? 8080, '0.0.0.0');
}
await bootstrap();
