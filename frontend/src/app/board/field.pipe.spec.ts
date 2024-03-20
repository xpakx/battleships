import { FieldPipe } from './field.pipe';

describe('FieldPipe', () => {
  let pipe: FieldPipe;

  beforeEach(() => {
    pipe = new FieldPipe();
  });

  it('transforms "Sunk" to "✘"', () => {
    const transformedValue = pipe.transform('Sunk');
    expect(transformedValue).toEqual('✘');
  });

  it('transforms "Miss" to "o"', () => {
    const transformedValue = pipe.transform('Miss');
    expect(transformedValue).toEqual('o');
  });

  it('transforms "Hit" to "."', () => {
    const transformedValue = pipe.transform('Hit');
    expect(transformedValue).toEqual('.');
  });

  it('transforms unknown value to empty string', () => {
    const transformedValue = pipe.transform('test');
    expect(transformedValue).toEqual('');
  });
});