import { Module } from '@nestjs/common';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Persona } from './personas/persona.entity.js';
import { PersonasModule } from './personas/personas.module.js';

@Module({
  imports: [
    ConfigModule.forRoot({ isGlobal: true }),
    TypeOrmModule.forRootAsync({
      inject: [ConfigService],
      useFactory: (config: ConfigService) => ({
        type: 'postgres',
        host: config.get('DB_HOST', 'localhost'),
        port: Number(config.get('DB_PORT', 5432)),
        username: config.get('DB_USER', 'postgres'),
        password: config.get('DB_PASS', 'password'),
        database: config.get('DB_NAME', 'personasdb'),
        entities: [Persona],
        synchronize: config.get('DB_SYNC', 'true') === 'true',
      }),
    }),
    PersonasModule,
  ],
})
export class AppModule {}
