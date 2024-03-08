import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'field'
})
export class FieldPipe implements PipeTransform {

  transform(value: String): String {
    // TODO

    if (value == "Sunk") {
      return "✘"
    }
    if (value == "Miss") {
      return "o"
    }
    if (value == "Hit") {
      return "."
    }
    return "";
  }

}
