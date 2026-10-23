import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Persona } from './persona.entity.js';
import { PersonasController } from './personas.controller.js';
import { PersonasService } from './personas.service.js';

@Module({
  imports: [TypeOrmModule.forFeature([Persona])],
  controllers: [PersonasController],
  providers: [PersonasService],
})
export class PersonasModule {}
