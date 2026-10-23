import {
  ConflictException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { unlink } from 'node:fs/promises';
import { join } from 'node:path';
import { Repository } from 'typeorm';
import { CreatePersonaDto, UpdatePersonaDto } from './dto/persona.dto.js';
import { Persona } from './persona.entity.js';

@Injectable()
export class PersonasService {
  constructor(
    @InjectRepository(Persona) private readonly repo: Repository<Persona>,
  ) {}

  async buscar(dni: string): Promise<Persona> {
    const p = await this.repo.findOneBy({ dni });
    if (!p) throw new NotFoundException(`No existe una persona con DNI ${dni}`);
    return p;
  }

  async crear(dto: CreatePersonaDto): Promise<Persona> {
    if (await this.repo.existsBy({ dni: dto.dni })) {
      throw new ConflictException(`Ya existe una persona con DNI ${dto.dni}`);
    }
    return this.repo.save(this.repo.create({ ...dto, fotoRuta: null }));
  }

  async actualizar(dni: string, dto: UpdatePersonaDto): Promise<Persona> {
    const p = await this.buscar(dni);
    return this.repo.save(this.repo.merge(p, dto));
  }

  async guardarFoto(dni: string, archivo: string): Promise<Persona> {
    const p = await this.buscar(dni);
    const anterior = p.fotoRuta;
    p.fotoRuta = `uploads/${archivo}`;
    const guardada = await this.repo.save(p);
    if (anterior)
      await unlink(join(process.cwd(), anterior)).catch(() => undefined);
    return guardada;
  }
}
