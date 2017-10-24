import ProtoLoader from '../core/ProtoLoader';
import Projection from '../domain/Projection';

export default class ProjectionMapper{
  static mapMode(mode) {
    switch (mode) {
      case Projection.Mode.PROJECTION: {
        return ProtoLoader.Dto.ProjectionConfigDto.Mode.Projection;
      }
      case Projection.Mode.INCLUSIONARY: {
        return ProtoLoader.Dto.ProjectionConfigDto.Mode.Inclusionary;
      }
      case Projection.Mode.EXCLUSIONARY: {
        return ProtoLoader.Dto.ProjectionConfigDto.Mode.Exclusionary;
      }
      default : {
        throw new Error(`Unable to map mode ${mode}`);
      }
    }
  }

  static toDto(projection) {
    return new ProtoLoader.Dto.ProjectionConfigDto(ProjectionMapper.mapMode(projection.mode), projection.projectionColumns);
  }
}
