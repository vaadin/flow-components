import '@polymer/polymer/lib/elements/custom-style.js';
const $_container = document.createElement('template');

$_container.innerHTML = `<style>
.box {
  display: flex;
  flex-direction: column;
  align-items: center;
  border: 3px solid #FFF;
  padding: 10px;
  background-color: #D3DCE0;
  font-size: 12px;

  .box__number.v-label-box__number {
    background-color: white;
    color: #9BA7A7;
    /* font-size: 16px; */
    border-radius: 50%;
    width: 28px;
    height: 28px;
    display: flex;
    align-items: center;
    margin-bottom: 8px;
    justify-content: center;
  }
}
</style>`;

document.head.appendChild($_container.content);